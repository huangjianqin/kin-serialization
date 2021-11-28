package org.kin.serialization.java;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.kin.framework.io.ByteBufferInputStream;
import org.kin.framework.io.ExpandableByteBufferOutputStream;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.serialization.OutputStreams;
import org.kin.serialization.Serialization;
import org.kin.serialization.SerializationType;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * @author 健勤
 * @date 2017/2/9
 */
@Extension(value = "java", code = 1)
public class JavaSerialization implements Serialization {
    @Override
    public byte[] serialize(Object target) {
        if (target == null) {
            throw new NullPointerException("Serialized object must be not null");
        }

        if (!(target instanceof Serializable)) {
            throw new IllegalStateException("Serialized class " + target.getClass().getSimpleName() + " must implement java.io.Serializable");
        }

        ByteArrayOutputStream baos = OutputStreams.getByteArrayOutputStream();
        try {
            serialize0(baos, target);
            return baos.toByteArray();
        }finally {
            OutputStreams.resetBuf(baos);
        }
    }

    @Override
    public <T> ByteBuffer serialize(ByteBuffer byteBuffer, T target) {
        ExpandableByteBufferOutputStream ebbos = new ExpandableByteBufferOutputStream(byteBuffer);
        serialize0(ebbos, target);
        return ebbos.getSink();
    }

    @Override
    public <T> void serialize(ByteBuf byteBuf, T target) {
        serialize0(new ByteBufOutputStream(byteBuf), target);
    }

    private <T> void serialize0(OutputStream os, T target){
        try (ObjectOutputStream oos = new ObjectOutputStream(os)){
            oos.writeObject(target);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> targetClass) {
        if (bytes == null || bytes.length <= 0) {
            throw new IllegalStateException("byte array must be not null or it's length must be greater than zero");
        }

        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
            return deserialize(is, targetClass);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    @Override
    public <T> T deserialize(ByteBuffer byteBuffer, Class<T> targetClass) {
        return deserialize(new ByteBufferInputStream(byteBuffer), targetClass);
    }

    @Override
    public <T> T deserialize(ByteBuf byteBuf, Class<T> targetClass) {
        return deserialize(new ByteBufInputStream(byteBuf), targetClass);
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize(InputStream is, Class<T> targetClass) {
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
