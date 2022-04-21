package org.kin.kinbuffer.io;

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
     * 读变长int32
     *
     * @return int
     */
    int readInt32();

    /**
     * 读有符号变长int32
     *
     * @return int
     */
    int readSInt32();

    /**
     * 读float
     *
     * @return float
     */
    float readFloat();

    /**
     * 读变长int64
     *
     * @return long
     */
    long readInt64();

    /**
     * 读有符号变长int64
     *
     * @return long
     */
    long readSInt64();

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
        int len = readInt32();
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
        }
        else if (len == 1) {
            return "";
        }

        byte[] bytes = readBytes(--len);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
