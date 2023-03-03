package org.kin.serialization.protobuf;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.util.internal.SystemPropertyUtil;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.kin.framework.io.ByteBufferUtils;
import org.kin.framework.io.ScalableByteBufferOutputStream;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.framework.utils.SysUtils;
import org.kin.serialization.AbstractSerialization;
import org.kin.serialization.SerializationType;
import org.kin.serialization.protobuf.io.*;
import org.kin.transport.netty.utils.ByteBufUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * protobuf与protostuff序列化结果有不同
 * 详细看{@link io.protostuff.LowCopyProtobufOutput#writeObject(int, Object, Schema, boolean)}和{@link io.protostuff.LowCopyProtostuffOutput#writeObject(int, Object, Schema, boolean)}
 *
 * 区别
 * 1. protobuf has an encoding type field named "group", and protostuff uses it as a nested message(w/c gives protostuff the ability to stream messages).
 *  added a runtime option for nested messages to be "group-encoded" for more efficient serialization. (especially for rpc).
 * 2. Protostuff can use the tail delimiter to process the message stream (Stream).
 * 3. The first-level class of protostuff supports circular references in the local format.
 *
 * @author huangjianqin
 * @date 2020/11/29
 */
@Extension(value = "protobuf", code = 5)
public final class ProtobufSerialization extends AbstractSerialization {
    static {
        // 默认 true, 禁止反序列化时构造方法被调用, 防止有些类的构造方法内有令人惊喜的逻辑
        String alwaysUseSunReflectionFactory = SystemPropertyUtil
                .get("kin.serialization.protostuff.always_use_sun_reflection_factory", "true");
        SysUtils.setProperty("protostuff.runtime.always_use_sun_reflection_factory", alwaysUseSunReflectionFactory);

        // Disabled by default.  Writes a sentinel value (uint32) in place of null values.
        // 默认 false, 不允许数组中的元素为 null
        String allowNullArrayElement = SystemPropertyUtil
                .get("kin.serialization.protostuff.allow_null_array_element", "false");
        SysUtils.setProperty("protostuff.runtime.allow_null_array_element", allowNullArrayElement);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> byte[] serialize0(T target) {
        if (target instanceof MessageLite) {
            //以protobuf序列化
            return Protobufs.serialize(target);
        } else {
            //以protostuff序列化
            Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(target.getClass());

            byte[] data;
            LinkedBuffer linkedBuffer = null;
            try {
                linkedBuffer = LinkedBuffers.getLinkedBuffer();
                data = ProtostuffIOUtil.toByteArray(target, schema, linkedBuffer);
            } finally {
                if (Objects.nonNull(linkedBuffer)) {
                    LinkedBuffers.clearBuffer(linkedBuffer);
                }
            }

            return data;
        }
    }

    @SuppressWarnings({"DuplicatedCode", "unchecked"})
    @Override
    protected <T> ByteBuffer serialize0(ByteBuffer byteBuffer, T target) {
        if (target instanceof MessageLite) {
            //以protobuf序列化
            ScalableByteBufferOutputStream sbbos = new ScalableByteBufferOutputStream(byteBuffer);
            Protobufs.serialize(sbbos, target);
            return sbbos.getSink();
        } else {
            //以protostuff序列化
            Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(target.getClass());

            Output output = Outputs.getOutput(byteBuffer);
            try {
                schema.writeTo(output, target);
            } catch (IOException e) {
                ExceptionUtils.throwExt(e);
            }
            return output.nioByteBuffer();
        }
    }

    @SuppressWarnings({"DuplicatedCode", "unchecked"})
    @Override
    protected <T> void serialize0(ByteBuf byteBuf, T target) {
        if (target instanceof MessageLite) {
            //以protobuf序列化
            Protobufs.serialize(new ByteBufOutputStream(byteBuf), target);
        } else {
            //以protostuff序列化
            Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(target.getClass());

            Output output = Outputs.getOutput(byteBuf);
            try {
                schema.writeTo(output, target);
                output.fixByteBufWriteIndex();
            } catch (IOException e) {
                ExceptionUtils.throwExt(e);
            }
        }
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> targetClass) {
        if (MessageLite.class.isAssignableFrom(targetClass)) {
            //以protobuf反序列化
            return Protobufs.deserialize(bytes, targetClass);
        } else {
            //以protostuff反序列化
            return protostuffDeserialize(Inputs.getInput(bytes), targetClass);
        }
    }

    @Override
    protected <T> T deserialize0(ByteBuffer byteBuffer, Class<T> targetClass) {
        if (MessageLite.class.isAssignableFrom(targetClass)) {
            //以protobuf反序列化
            return Protobufs.deserialize(byteBuffer, targetClass);
        } else {
            //以protostuff反序列化
            return protostuffDeserialize(Inputs.getInput(byteBuffer), targetClass);
        }
    }

    @Override
    protected <T> T deserialize0(ByteBuf byteBuf, Class<T> targetClass) {
        if (MessageLite.class.isAssignableFrom(targetClass)) {
            //以protobuf反序列化
            return Protobufs.deserialize(byteBuf, targetClass);
        } else {
            //以protostuff反序列化
            Input input = Inputs.getInput(byteBuf);
            T result = protostuffDeserialize(input, targetClass);
            input.fixByteBufReadIndex();
            return result;
        }
    }

    /**
     * 以protostuff反序列化
     */
    private <T> T protostuffDeserialize(io.protostuff.Input input, Class<T> targetClass) {
        Schema<T> schema = RuntimeSchema.getSchema(targetClass);
        T obj = schema.newMessage();

        try {
            schema.mergeFrom(input, obj);
            Inputs.checkEnd(input);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        return obj;
    }

    @Override
    public int type() {
        return SerializationType.PROTOBUF.getCode();
    }
}
