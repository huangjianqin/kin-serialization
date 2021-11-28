package org.kin.serialization.protobuf;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.SystemPropertyUtil;
import io.protostuff.Input;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.kin.framework.io.ByteBufferUtils;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.framework.utils.SysUtils;
import org.kin.serialization.Serialization;
import org.kin.serialization.SerializationType;
import org.kin.serialization.protobuf.io.Inputs;
import org.kin.serialization.protobuf.io.LinkedBuffers;
import org.kin.serialization.protobuf.io.Output;
import org.kin.serialization.protobuf.io.Outputs;
import org.kin.transport.netty.utils.ByteBufUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * !!!!!
 * {@link #serialize(Object)}使用protostuff原生的变长整形算法
 * {@link #serialize(ByteBuf, Object)}和{@link #serialize(ByteBuffer, Object)}使用基于zigzag的变长整形算法
 * 所以序列化后的bytes存在差异, 切莫互用
 *
 * @author huangjianqin
 * @date 2020/11/29
 */
@Extension(value = "protobuf", code = 5)
public class ProtobufSerialization implements Serialization {
    static {
        // 默认 true, 禁止反序列化时构造方法被调用, 防止有些类的构造方法内有令人惊喜的逻辑
        String alwaysUseSunReflectionFactory = SystemPropertyUtil
                .get("kin.serialization.protostuff.always_use_sun_reflection_factory", "true");
        SysUtils.setProperty("protostuff.runtime.always_use_sun_reflection_factory", alwaysUseSunReflectionFactory);

        // Disabled by default.  Writes a sentinel value (uint32) in place of null values.
        // 默认 false, 不允许数组中的元素为 null
        String allowNullArrayElement = SystemPropertyUtil
                .get("jupiter.serializer.protostuff.allow_null_array_element", "false");
        SysUtils.setProperty("protostuff.runtime.allow_null_array_element", allowNullArrayElement);
    }

    /**
     * 检查{@code target}是否为null
     */
    private void checkNull(Object target) {
        if (Objects.isNull(target)) {
            throw new IllegalStateException("serialize object is null");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> byte[] serialize(T target) {
        checkNull(target);

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
    public <T> ByteBuffer serialize(ByteBuffer buffer, T target) {
        checkNull(target);

        if (target instanceof MessageLite) {
            //以protobuf序列化
            byte[] bytes = Protobufs.serialize(target);
            ByteBuffer ret = ByteBufferUtils.ensureWritableBytes(buffer, bytes.length);
            ret.put(bytes);
            return ret;
        } else {
            //以protostuff序列化
            Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(target.getClass());

            Output output = Outputs.getOutput(buffer);
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
    public <T> void serialize(ByteBuf byteBuf, T target) {
        checkNull(target);

        if (target instanceof MessageLite) {
            //以protobuf序列化
            byteBuf.writeBytes(Protobufs.serialize(target));
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
    public <T> T deserialize(byte[] bytes, Class<T> targetClass) {
        if (MessageLite.class.isAssignableFrom(targetClass)) {
            //以protobuf反序列化
            return Protobufs.deserialize(bytes, targetClass);
        } else {
            //以protostuff反序列化
            return protostuffDeserialize(Inputs.getInput(bytes), targetClass);
        }
    }

    @Override
    public <T> T deserialize(ByteBuffer buffer, Class<T> targetClass) {
        if (MessageLite.class.isAssignableFrom(targetClass)) {
            //以protobuf反序列化
            return Protobufs.deserialize(ByteBufferUtils.toBytes(buffer), targetClass);
        } else {
            //以protostuff反序列化
            return protostuffDeserialize(Inputs.getInput(buffer), targetClass);
        }
    }

    @Override
    public <T> T deserialize(ByteBuf buffer, Class<T> targetClass) {
        if (MessageLite.class.isAssignableFrom(targetClass)) {
            //以protobuf反序列化
            return Protobufs.deserialize(ByteBufUtils.toBytes(buffer), targetClass);
        } else {
            //以protostuff反序列化
            return protostuffDeserialize(Inputs.getInput(buffer), targetClass);
        }
    }

    /**
     * 以protostuff反序列化
     */
    private <T> T protostuffDeserialize(Input input, Class<T> targetClass) {
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
