package org.kin.serialization.java;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.kin.framework.io.ByteBufferInputStream;
import org.kin.framework.io.ScalableByteBufferOutputStream;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.serialization.AbstractSerialization;
import org.kin.serialization.OutputStreams;
import org.kin.serialization.SerializationType;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * @author 健勤
 * @date 2017/2/9
 */
@Extension(value = "java", code = 1)
public final class JavaSerialization extends AbstractSerialization {
    @Override
    protected byte[] serialize0(Object target) {
        ByteArrayOutputStream baos = OutputStreams.getByteArrayOutputStream();
        try {
            serialize1(baos, target);
            return baos.toByteArray();
        } finally {
            OutputStreams.resetBuf(baos);
        }
    }

    @Override
    protected <T> ByteBuffer serialize0(ByteBuffer byteBuffer, T target) {
        ScalableByteBufferOutputStream ebbos = new ScalableByteBufferOutputStream(byteBuffer);
        serialize1(ebbos, target);
        return ebbos.getSink();
    }

    @Override
    protected <T> void serialize0(ByteBuf byteBuf, T target) {
        serialize1(new ByteBufOutputStream(byteBuf), target);
    }

    private <T> void serialize1(OutputStream os, T target) {
        try (ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(target);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
    }

    @Override
    public <T> T deserialize0(byte[] bytes, Class<T> targetClass) {
        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
            return deserialize1(is);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    @Override
    protected <T> T deserialize0(ByteBuffer byteBuffer, Class<T> targetClass) {
        return deserialize1(new ByteBufferInputStream(byteBuffer));
    }

    @Override
    protected <T> T deserialize0(ByteBuf byteBuf, Class<T> targetClass) {
        return deserialize1(new ByteBufInputStream(byteBuf));
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize1(InputStream is) {
        try (ObjectInputStream ois = new ObjectInputStream(is)) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    @Override
    public int type() {
        return SerializationType.JAVA.getCode();
    }
}
