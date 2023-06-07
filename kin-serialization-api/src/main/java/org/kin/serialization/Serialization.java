package org.kin.serialization;


import io.netty.buffer.ByteBuf;
import org.kin.framework.utils.CollectionUtils;
import org.kin.framework.utils.SPI;

import java.nio.ByteBuffer;

/**
 * Created by 健勤 on 2017/2/10.
 */
@SPI(alias = "serialization", coded = true)
public interface Serialization {
    /** {@link Serialization}最大缓存buffer size */
    int MAX_CACHED_BUF_SIZE = 256 * 1024;
    /** {@link Serialization}默认buffer size */
    int DEFAULT_BUFFER_SIZE = 512;
    /** 空的字节数组 */
    byte[] EMPTY_BYTES = new byte[0];
    /** 空的{@link ByteBuffer} */
    ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.allocateDirect(0);
    /** 空的Object数组 */
    Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * 序列化
     *
     * @param target 实例
     * @return 序列化后的字节数组
     */
    <T> byte[] serialize(T target);

    /**
     * 序列化Object[], 默认仅支持序列化一个对象实例
     *
     * @param objects 实例数组
     * @return 序列化后的字节数组
     */
    byte[] serialize(Object[] objects);


    /**
     * 序列化
     *
     * @param target     实例
     * @param byteBuffer 序列化后的字节写入的java byte byteBuffer
     */
    <T> ByteBuffer serialize(ByteBuffer byteBuffer, T target);

    /**
     * 序列化Object[], 默认仅支持序列化一个对象实例
     *
     * @param objects 实例数组
     * @return 序列化后的字节数组
     */
    ByteBuffer serialize(ByteBuffer byteBuffer, Object[] objects);

    /**
     * 序列化
     *
     * @param target  实例
     * @param byteBuf 序列化后的字节写入的netty byte buffer
     */
    <T> void serialize(ByteBuf byteBuf, T target);

    /**
     * 序列化Object[], 默认仅支持序列化一个对象实例
     *
     * @param objects 实例数组
     * @return 序列化后的字节数组
     */
    void serialize(ByteBuf byteBuf, Object[] objects);

    /**
     * 反序列化
     *
     * @param bytes       字节数组
     * @param targetClass 指定类
     * @param <T>         指定类
     * @return 反序列化结果
     */
    <T> T deserialize(byte[] bytes, Class<T> targetClass);

    /**
     * 反序列化
     *
     * @param bytes       字节数组
     * @param targetClasses 指定多个类
     */
    Object[] deserialize(byte[] bytes, Class<?>... targetClasses);

    /**
     * 反序列化
     *
     * @param byteBuffer  java byte byteBuffer, 要保证是读模式, 不校验
     * @param targetClass 指定类
     * @param <T>         指定类
     * @return 反序列化结果
     */
    <T> T deserialize(ByteBuffer byteBuffer, Class<T> targetClass);

    /**
     * 反序列化
     *
     * @param byteBuffer  java byte byteBuffer, 要保证是读模式, 不校验
     * @param targetClasses 指定多个类
     */
    Object[] deserialize(ByteBuffer byteBuffer, Class<?>... targetClasses);

    /**
     * 反序列化
     *
     * @param byteBuf     netty byte byteBuf
     * @param targetClass 指定类
     * @param <T>         指定类
     * @return 反序列化结果
     */
    <T> T deserialize(ByteBuf byteBuf, Class<T> targetClass);

    /**
     * 反序列化
     *
     * @param byteBuf     netty byte byteBuf
     * @param targetClasses 指定多个类
     */
    Object[] deserialize(ByteBuf byteBuf, Class<?>... targetClasses);

    /**
     * @return 序列化类型code, 必须>0
     */
    int type();
}
