package org.kin.serialization;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import org.kin.framework.io.ByteBufferUtils;
import org.kin.framework.utils.CollectionUtils;
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
    public final byte[] serialize(Object[] objects) {
        if(CollectionUtils.isEmpty(objects)){
            return EMPTY_BYTES;
        }else if(objects.length == 1){
            return serialize(objects[0]);
        }
        else{
            return serializeMany(objects);
        }
    }

    /**
     * 同时序列化多个对象实例
     */
    protected byte[] serializeMany(Object[] objects){
        //默认不支持同时序列化多个对象实例
        throw new UnsupportedOperationException("just only support serialize one object");
    }

    @Override
    public final <T> ByteBuffer serialize(ByteBuffer byteBuffer, T target) {
        serializeCheck(target, byteBuffer);
        return serialize0(byteBuffer, target);
    }

    protected <T> ByteBuffer serialize0(ByteBuffer byteBuffer, T target) {
        //默认实现, 会存在多一次bytes复制, 建议尽可能重写
        byte[] bytes = serialize(target);
        ByteBuffer ret = ByteBufferUtils.ensureWritableBytes(byteBuffer, bytes.length);
        ret.put(bytes);
        return ret;
    }

    @Override
    public final ByteBuffer serialize(ByteBuffer byteBuffer, Object[] objects) {
        if(CollectionUtils.isEmpty(objects)){
            return EMPTY_BYTE_BUFFER;
        }else if(objects.length == 1){
            return serialize(byteBuffer, objects[0]);
        }
        else{
            return serializeMany(byteBuffer, objects);
        }
    }

    /**
     * 同时序列化多个对象实例
     */
    protected ByteBuffer serializeMany(ByteBuffer byteBuffer, Object[] objects){
        //默认不支持同时序列化多个对象实例
        throw new UnsupportedOperationException("just only support serialize one object");
    }

    @Override
    public final <T> void serialize(ByteBuf byteBuf, T target) {
        serializeCheck(target, byteBuf);
        serialize0(byteBuf, target);
    }

    protected <T> void serialize0(ByteBuf byteBuf, T target) {
        //默认实现, 会存在多一次bytes复制, 建议尽可能重写
        byteBuf.writeBytes(serialize(target));
    }

    @Override
    public final void serialize(ByteBuf byteBuf, Object[] objects) {
        if(CollectionUtils.isEmpty(objects)){
            //do nothing
        }else if(objects.length == 1){
            serialize(byteBuf, objects[0]);
        }
        else{
            serializeMany(byteBuf, objects);
        }
    }

    /**
     * 同时序列化多个对象实例
     */
    protected void serializeMany(ByteBuf byteBuf, Object[] objects){
        //默认不支持同时序列化多个对象实例
        throw new UnsupportedOperationException("just only support serialize one object");
    }

    /**
     * 反序列化前检查
     */
    protected <T> void checkBytes(byte[] bytes) {
        Preconditions.checkNotNull(bytes);
        if (bytes.length == 0) {
            throw new IllegalStateException("byte array must be not null or it's length must be greater than zero");
        }
    }

    @Override
    public final <T> T deserialize(byte[] bytes, Class<T> targetClass) {
        Preconditions.checkNotNull(targetClass);
        checkBytes(bytes);
        return deserialize0(bytes, targetClass);
    }

    protected abstract <T> T deserialize0(byte[] bytes, Class<T> targetClass);

    @Override
    public final Object[] deserialize(byte[] bytes, Class<?>... targetClasses) {
        checkBytes(bytes);
        if(CollectionUtils.isEmpty(targetClasses)){
            return EMPTY_OBJECT_ARRAY;
        }else if(targetClasses.length == 1){
            return new Object[]{deserialize(bytes, targetClasses[0])};
        }
        else{
            return deserializeMany(bytes, targetClasses);
        }
    }

    /**
     * 同时序列化多个对象实例
     */
    protected Object[] deserializeMany(byte[] bytes, Class<?>... targetClasses){
        //默认不支持同时序列化多个对象实例
        throw new UnsupportedOperationException("just only support deserialize one object");
    }

    /**
     * 反序列化前检查
     */
    protected <T> void checkByteBuffer(ByteBuffer buffer) {
        Preconditions.checkNotNull(buffer);
        if (!buffer.hasRemaining()) {
            throw new IllegalStateException("byte buffer must be not null or it's length must be greater than zero");
        }
    }

    @Override
    public final <T> T deserialize(ByteBuffer byteBuffer, Class<T> targetClass) {
        Preconditions.checkNotNull(targetClass);
        checkByteBuffer(byteBuffer);
        return deserialize0(byteBuffer, targetClass);
    }

    protected <T> T deserialize0(ByteBuffer byteBuffer, Class<T> targetClass) {
        //默认实现
        return deserialize(ByteBufferUtils.toBytes(byteBuffer), targetClass);
    }

    @Override
    public final Object[] deserialize(ByteBuffer buffer, Class<?>... targetClasses) {
        checkByteBuffer(buffer);
        if(CollectionUtils.isEmpty(targetClasses)){
            return EMPTY_OBJECT_ARRAY;
        }else if(targetClasses.length == 1){
            return new Object[]{deserialize(buffer, targetClasses[0])};
        }
        else{
            return deserializeMany(buffer, targetClasses);
        }
    }

    /**
     * 同时序列化多个对象实例
     */
    protected Object[] deserializeMany(ByteBuffer byteBuffer, Class<?>... targetClasses){
        //默认不支持同时序列化多个对象实例
        throw new UnsupportedOperationException("just only support deserialize one object");
    }

    /**
     * 反序列化前检查
     */
    protected <T> void checkByteBuf(ByteBuf byteBuf) {
        Preconditions.checkNotNull(byteBuf);
        if (byteBuf.readableBytes() < 1) {
            throw new IllegalStateException("bytebuf must be not null or it's length must be greater than zero");
        }
    }

    @Override
    public final <T> T deserialize(ByteBuf byteBuf, Class<T> targetClass) {
        Preconditions.checkNotNull(targetClass);
        checkByteBuf(byteBuf);
        return deserialize0(byteBuf, targetClass);
    }

    protected <T> T deserialize0(ByteBuf byteBuf, Class<T> targetClass) {
        //默认实现
        return deserialize(ByteBufUtils.toBytes(byteBuf), targetClass);
    }

    @Override
    public final Object[] deserialize(ByteBuf byteBuf, Class<?>... targetClasses) {
        checkByteBuf(byteBuf);
        if(CollectionUtils.isEmpty(targetClasses)){
            return EMPTY_OBJECT_ARRAY;
        }else if(targetClasses.length == 1){
            return new Object[]{deserialize(byteBuf, targetClasses[0])};
        }
        else{
            return deserializeMany(byteBuf, targetClasses);
        }
    }

    /**
     * 同时序列化多个对象实例
     */
    protected Object[] deserializeMany(ByteBuf byteBuf, Class<?>... targetClasses){
        //默认不支持同时序列化多个对象实例
        throw new UnsupportedOperationException("just only support deserialize one object");
    }
}
