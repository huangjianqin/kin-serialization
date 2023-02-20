package org.kin.serialization.kinbuffer;

import io.netty.buffer.ByteBuf;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.kinbuffer.io.*;
import org.kin.kinbuffer.runtime.Runtime;
import org.kin.kinbuffer.runtime.Schema;
import org.kin.serialization.AbstractSerialization;
import org.kin.serialization.SerializableClassRegistry;
import org.kin.serialization.SerializationType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2021/12/25
 */
@SuppressWarnings("unchecked")
@Extension(value = "kinbuffer", code = 8)
public class KinbufferSerialization extends AbstractSerialization {

    public KinbufferSerialization() {
        for (Map.Entry<Class<?>, Object> entry : SerializableClassRegistry.getRegisteredClasses().entrySet()) {
            Class<?> claxx = entry.getKey();
            Object serializer = entry.getValue();
            if (Objects.nonNull(serializer)) {
                //noinspection rawtypes
                Runtime.registerClass(claxx, (Schema) serializer);
            }
            else{
                Runtime.registerClass(Runtime.nextMessageId(), claxx);
            }
        }
    }

    @Override
    protected <T> byte[] serialize0(T target) {
        ByteArrayOutput output = Outputs.getOutput();
        try {
            serialize1(output, target);
            return output.toByteArray();
        } finally {
            Outputs.clearByteArrayOutput();
        }
    }

    @Override
    protected <T> ByteBuffer serialize0(ByteBuffer byteBuffer, T target) {
        NioBufOutput output = Outputs.getOutput(byteBuffer);
        serialize1(output, target);
        return output.getByteBuffer();
    }

    @Override
    protected <T> void serialize0(ByteBuf byteBuf, T target) {
        NioBufOutput output = Outputs.getOutput(byteBuf);
        serialize1(output, target);
        output.fixByteBufWriteIndex();
    }

    private <T> void serialize1(Output output, T target) {
        Schema<T> schema = (Schema<T>) Runtime.getSchema(target.getClass());
        schema.write(output, target);
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> targetClass) {
        return deserialize1(Inputs.getInput(bytes), targetClass);
    }

    @Override
    protected <T> T deserialize0(ByteBuffer byteBuffer, Class<T> targetClass) {
        return deserialize1(Inputs.getInput(byteBuffer), targetClass);
    }

    @Override
    protected <T> T deserialize0(ByteBuf byteBuf, Class<T> targetClass) {
        return deserialize1(Inputs.getInput(byteBuf), targetClass);
    }

    /**
     * 反序列化统一处理
     */
    private <T> T deserialize1(Input input, Class<T> targetClass) {
        Schema<T> schema = Runtime.getSchema(targetClass);
        T message = schema.newMessage();
        schema.merge(input, message);
        return message;
    }

    @Override
    public int type() {
        return SerializationType.KIN_BUFFER.getCode();
    }
}
