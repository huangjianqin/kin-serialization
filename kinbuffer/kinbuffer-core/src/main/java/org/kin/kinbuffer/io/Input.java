package org.kin.kinbuffer.io;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * 从buffer读取序列化的bytes
 *
 * @author huangjianqin
 * @date 2021/12/12
 */
public interface Input {
    /**
     * 读boolean
     *
     * @return boolean
     */
    boolean readBoolean();

    /**
     * 读bytes
     *
     * @param len bytes长度
     */
    byte[] readBytes(int len);

    /**
     * 读Bytes
     *
     * @return 可读的所有Bytes
     */
    byte[] readBytes();

    /**
     * 读byte
     *
     * @return byte
     */
    byte readByte();

    /**
     * 读固定int32
     *
     * @return int
     */
    int readInt32();

    /**
     * 读无符号固定int32
     *
     * @return int
     */
    long readUInt32();

    /**
     * 读有符号固定int32
     *
     * @return int
     */
    int readSInt32();

    /**
     * 读变长int32
     *
     * @return int
     */
    int readVarInt32();

    /**
     * 读有符号变长int32
     *
     * @return int
     */
    int readSVarInt32();

    /**
     * 读float
     *
     * @return float
     */
    float readFloat();

    /**
     * 读固定int64
     *
     * @return long
     */
    long readInt64();

    /**
     * 读有符号固定int64
     *
     * @return long
     */
    long readSInt64();

    /**
     * 读变长int64
     *
     * @return long
     */
    long readVarInt64();

    /**
     * 读有符号变长int64
     *
     * @return long
     */
    long readSVarInt64();

    /**
     * 读double
     *
     * @return double
     */
    double readDouble();

    /**
     * 读String
     *
     * @return String
     */
    default String readString() {
        int len = readVarInt32();
        return readString(len);
    }

    /**
     * 读String
     *
     * @param len bytes len
     * @return String
     */
    default String readString(int len) {
        if (len == 0) {
            return null;
        } else if (len == 1) {
            return "";
        }

        byte[] bytes = readBytes(--len);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 修正{@link ByteBuf#readerIndex()}
     * 因为底层使用{@link ByteBuf#nioBuffer()}获取{@link java.nio.ByteBuffer}实例,
     * 但修改{@link java.nio.ByteBuffer}实例, 对{@link io.netty.buffer.ByteBuf}不可见,
     * 故完成output后需要修正{@link ByteBuf#readerIndex()}
     */
    default void fixReadIndex() {
        throw new UnsupportedOperationException("inputBuf is null, so this method is not supported");
    }
}
