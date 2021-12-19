package org.kin.kinbuffer.io;

import java.nio.charset.StandardCharsets;

/**
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
     * @param length bytes长度
     */
    byte[] readBytes(int length);

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
    int readByte();

    /**
     * 读int
     *
     * @return int
     */
    int readInt();

    /**
     * 读float
     *
     * @return float
     */
    float readFloat();

    /**
     * 读long
     *
     * @return long
     */
    long readLong();

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
    default String readString(){
        int len = readInt();
        byte[] bytes = readBytes(len);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
