package org.kin.serialization.kinbuffer;

import io.netty.buffer.ByteBuf;
import org.kin.framework.io.ScalableByteBufferOutputStream;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.kinbuffer.io.DefaultInput;
import org.kin.kinbuffer.io.DefaultOutput;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.runtime.Runtime;
import org.kin.kinbuffer.runtime.Schema;
import org.kin.serialization.AbstractSerialization;
import org.kin.serialization.OutputStreams;
import org.kin.serialization.SerializationType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author huangjianqin
 * @date 2021/12/25
 */
@SuppressWarnings("unchecked")
@Extension(value = "kinbuffer", code = 8)
public class KinbufferSerialization extends AbstractSerialization {

    @Override
    protected <T> byte[] serialize0(T target) {
        ByteArrayOutputStream baos = OutputStreams.getByteArrayOutputStream();
        try {
            serialize1(DefaultOutput.stream(baos), target);
            return baos.toByteArray();
        } finally {
            OutputStreams.resetBuf(baos);
        }
    }

    @Override
    protected <T> ByteBuffer serialize0(ByteBuffer byteBuffer, T target) {
        ScalableByteBufferOutputStream ebbos = new ScalableByteBufferOutputStream(byteBuffer);
        serialize1(DefaultOutput.stream(ebbos), target);
        return ebbos.getSink();
    }

    @Override
    protected <T> void serialize0(ByteBuf byteBuf, T target) {
        serialize1(DefaultOutput.buffer(byteBuf), target);
    }

    private <T> void serialize1(DefaultOutput output, T target) {
        Schema<T> schema = (Schema<T>) Runtime.getSchema(target.getClass());
        schema.write(output, target);
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> targetClass) {
        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
            return deserialize1(DefaultInput.stream(is), targetClass);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    @Override
    protected <T> T deserialize0(ByteBuffer byteBuffer, Class<T> targetClass) {
        return deserialize1(DefaultInput.buffer(byteBuffer), targetClass);
    }

    @Override
    protected <T> T deserialize0(ByteBuf byteBuf, Class<T> targetClass) {
        return deserialize1(DefaultInput.buffer(byteBuf), targetClass);
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
