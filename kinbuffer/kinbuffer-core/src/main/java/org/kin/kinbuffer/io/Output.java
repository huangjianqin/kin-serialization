package org.kin.kinbuffer.io;

import java.nio.charset.StandardCharsets;

/**
 * @author huangjianqin
 * @date 2021/12/12
 */
public interface Output {
    /**
     * 写boolean
     *
     * @param value 值
     * @return Output
     */
    Output writeBoolean(boolean value);

    /**
     * 写bytes
     *
     * @param value 值
     * @return Output
     */
    Output writeBytes(byte[] value);

    /**
     * 写byte
     *
     * @param value 值
     * @return Output
     */
    Output writeByte(int value);

    /**
     * 写int
     *
     * @param value 值
     * @return Output
     */
    Output writeInt(int value);

    /**
     * 写float
     *
     * @param value 值
     * @return Output
     */
    Output writeFloat(float value);

    /**
     * 写long
     *
     * @param value 值
     * @return Output
     */
    Output writeLong(long value);

    /**
     * 写double
     *
     * @param value 值
     * @return Output
     */
    Output writeDouble(double value);

    /**
     * 写String
     *
     * @param value 值
     * @return Output
     */
    default Output writeString(String value){
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeInt(bytes.length);
        writeBytes(bytes);
        return this;
    }
}
