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
     * 写变长int32
     *
     * @param value 值
     * @return Output
     */
    Output writeInt32(int value);

    /**
     * 写有符号变长int32
     *
     * @param value 值
     * @return Output
     */
    Output writeSInt32(int value);

    /**
     * 写float
     *
     * @param value 值
     * @return Output
     */
    Output writeFloat(float value);

    /**
     * 写变长int64
     *
     * @param value 值
     * @return Output
     */
    Output writeInt64(long value);

    /**
     * 写有符号变长int64
     *
     * @param value 值
     * @return Output
     */
    Output writeSInt64(int value);

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
        writeInt32(bytes.length);
        writeBytes(bytes);
        return this;
    }
}
