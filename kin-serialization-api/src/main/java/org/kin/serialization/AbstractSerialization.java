package org.kin.serialization;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import org.kin.framework.io.ByteBufferUtils;
import org.kin.transport.netty.utils.ByteBufUtils;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * 抽象的{@link Serialization}, 作统一参数校验
 *
 * @author huangjianqin
 * @date 2021/11/28
 */
public abstract class AbstractSerialization implements Serialization {
    /**
     * 序列化前检查
     *
     * @param bufs byte buffer实例
     */
    protected <T> void serializeCheck(T target, Object... bufs) {
        if (target == null) {
            throw new NullPointerException("Serialized object must be not null");
        }

        if (!(target instanceof Serializable)) {
            throw new IllegalStateException("Serialized class " + target.getClass().getSimpleName() + " must implement java.io.Serializable");
        }

        for (Object buf : bufs) {
            Preconditions.checkNotNull(buf);
        }
    }

    @Override
    public final <T> byte[] serialize(T target) {
        serializeCheck(target);
        return serialize0(target);
    }

    protected abstract <T> byte[] serialize0(T target);

    @Override
    public final <T> ByteBuffer serialize(ByteBuffer byteBuffer, T target) {
        serializeCheck(target, byteBuffer);
        return serialize0(byteBuffer, target);
    }

    protected <T> ByteBuffer serialize0(ByteBuffer byteBuffer, T target) {
        //默认实现
        byte[] bytes = serialize(target);
        ByteBuffer ret = ByteBufferUtils.ensureWritableBytes(byteBuffer, bytes.length);
        ret.put(bytes);
        return ret;
    }

    @Override
    public final <T> void serialize(ByteBuf byteBuf, T target) {
        serializeCheck(target, byteBuf);
        serialize0(byteBuf, target);
    }

    protected <T> void serialize0(ByteBuf byteBuf, T target) {
        //默认实现
        byteBuf.writeBytes(serialize(target));
    }

    /**
     * 反序列化前检查
     */
    protected <T> void deserializeCheck(byte[] bytes, Class<T> targetClass) {
        Preconditions.checkNotNull(targetClass);
        Preconditions.checkNotNull(bytes);
        if (bytes.length <= 0) {
            throw new IllegalStateException("byte array must be not null or it's length must be greater than zero");
        }
    }

    @Override
    public final <T> T deserialize(byte[] bytes, Class<T> targetClass) {
        deserializeCheck(bytes, targetClass);
        return deserialize0(bytes, targetClass);
    }

    protected abstract <T> T deserialize0(byte[] bytes, Class<T> targetClass);

    /**
     * 反序列化前检查
     */
    protected <T> void deserializeCheck(ByteBuffer buffer, Class<T> targetClass) {
        Preconditions.checkNotNull(targetClass);
        Preconditions.checkNotNull(buffer);
        if (!buffer.hasRemaining()) {
            throw new IllegalStateException("byte buffer must be not null or it's length must be greater than zero");
        }
    }

    @Override
    public final <T> T deserialize(ByteBuffer byteBuffer, Class<T> targetClass) {
        deserializeCheck(byteBuffer, targetClass);
        return deserialize0(byteBuffer, targetClass);
    }

    protected <T> T deserialize0(ByteBuffer byteBuffer, Class<T> targetClass) {
        //默认实现
        return deserialize(ByteBufferUtils.toBytes(byteBuffer), targetClass);
    }

    /**
     * 反序列化前检查
     */
    protected <T> void deserializeCheck(ByteBuf byteBuf, Class<T> targetClass) {
        Preconditions.checkNotNull(targetClass);
        Preconditions.checkNotNull(byteBuf);
        if (byteBuf.readableBytes() < 1) {
            throw new IllegalStateException("bytebuf must be not null or it's length must be greater than zero");
        }
    }

    @Override
    public final <T> T deserialize(ByteBuf byteBuf, Class<T> targetClass) {
        deserializeCheck(byteBuf, targetClass);
        return deserialize0(byteBuf, targetClass);
    }

    protected <T> T deserialize0(ByteBuf byteBuf, Class<T> targetClass) {
        //默认实现
        return deserialize(ByteBufUtils.toBytes(byteBuf), targetClass);
    }
}
