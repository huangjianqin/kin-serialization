package org.kin.kinbuffer.io;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 将序列化后的bytes输出到buffer
 *
 * @author huangjianqin
 * @date 2021/12/12
 */
public interface Output {
    /**
     * 写boolean
     *
     * @param bool 值
     * @return Output
     */
    Output writeBoolean(boolean bool);

    /**
     * 写bytes
     *
     * @param bytes 值
     * @return Output
     */
    Output writeBytes(byte[] bytes);

    /**
     * 写byte
     *
     * @param b 值
     * @return Output
     */
    Output writeByte(int b);

    /**
     * 写变长int32
     *
     * @param i 值
     * @return Output
     */
    Output writeInt32(int i);

    /**
     * 写有符号变长int32
     *
     * @param si 值
     * @return Output
     */
    Output writeSInt32(int si);

    /**
     * 写float
     *
     * @param f 值
     * @return Output
     */
    Output writeFloat(float f);

    /**
     * 写变长int64
     *
     * @param l 值
     * @return Output
     */
    Output writeInt64(long l);

    /**
     * 写有符号变长int64
     *
     * @param sl 值
     * @return Output
     */
    Output writeSInt64(long sl);

    /**
     * 写double
     *
     * @param d 值
     * @return Output
     */
    Output writeDouble(double d);

    /**
     * 写String
     *
     * @param s 值
     * @return Output
     */
    default Output writeString(String s){
        if(Objects.isNull(s)){
            //null
            writeInt32(0);
            return this;
        }

        if(s.isEmpty()){
            //blank
            writeInt32(1);
            return this;
        }

        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        int len = bytes.length;

        writeInt32(len);
        writeBytes(bytes);
        return this;
    }
}
