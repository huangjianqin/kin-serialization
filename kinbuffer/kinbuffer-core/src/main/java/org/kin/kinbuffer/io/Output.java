package org.kin.kinbuffer.io;

import io.netty.buffer.ByteBuf;

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
     * 写固定int32
     *
     * @param i 值
     * @return Output
     */
    Output writeInt32(int i);

    /**
     * 写无符号固定int32
     *
     * @param i 值
     * @return Output
     */
    Output writeUInt32(int i);

    /**
     * 写有符号固定int32
     *
     * @param si 值
     * @return Output
     */
    Output writeSInt32(int si);

    /**
     * 写变长int32
     *
     * @param i 值
     * @return Output
     */
    Output writeVarInt32(int i);

    /**
     * 写有符号变长int32
     *
     * @param si 值
     * @return Output
     */
    Output writeSVarInt32(int si);

    /**
     * 写float
     *
     * @param f 值
     * @return Output
     */
    Output writeFloat(float f);

    /**
     * 写固定int64
     *
     * @param l 值
     * @return Output
     */
    Output writeInt64(long l);

    /**
     * 写有符号固定int64
     *
     * @param sl 值
     * @return Output
     */
    Output writeSInt64(long sl);

    /**
     * 写变长int64
     *
     * @param l 值
     * @return Output
     */
    Output writeVarInt64(long l);

    /**
     * 写有符号变长int64
     *
     * @param sl 值
     * @return Output
     */
    Output writeSVarInt64(long sl);

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
            writeVarInt32(0);
            return this;
        }

        if(s.isEmpty()){
            //blank
            writeVarInt32(1);
            return this;
        }

        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        int len = bytes.length;

        writeVarInt32(len);
        writeBytes(bytes);
        return this;
    }

    /**
     * 修正{@link ByteBuf#writerIndex()}
     * 因为底层使用{@link ByteBuf#nioBuffer()}获取{@link java.nio.ByteBuffer}实例,
     * 但修改{@link java.nio.ByteBuffer}实例, 对{@link io.netty.buffer.ByteBuf}不可见,
     * 故完成output后需要修正{@link ByteBuf#writerIndex()}
     */
     default void fixWriteIndex() {
         throw new UnsupportedOperationException("outputBuf is null, so this method is not supported");
     }
}
