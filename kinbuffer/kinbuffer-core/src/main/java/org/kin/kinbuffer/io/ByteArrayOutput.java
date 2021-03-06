package org.kin.kinbuffer.io;

import org.kin.framework.io.ScalableByteArray;
import org.kin.framework.utils.BytesUtils;
import org.kin.framework.utils.UnsafeUtf8Util;
import org.kin.framework.utils.UnsafeUtil;
import org.kin.framework.utils.VarIntUtils;

import java.util.Objects;

import static java.lang.Character.*;

/**
 * @author huangjianqin
 * @date 2022/4/15
 */
public final class ByteArrayOutput implements Output{
    private final ScalableByteArray array;

    public ByteArrayOutput() {
        this(new ScalableByteArray());
    }

    public ByteArrayOutput(ScalableByteArray array) {
        this.array = array;
    }

    @Override
    public Output writeBoolean(boolean bool) {
        array.writeByte(bool ? 1 : 0);
        return this;
    }

    @Override
    public Output writeBytes(byte[] bytes) {
        array.writeBytes(bytes);
        return this;
    }

    @Override
    public Output writeByte(int b) {
        array.writeByte(b);
        return this;
    }

    @Override
    public Output writeInt32(int i) {
        VarIntUtils.writeRawVarInt32(array, i);
        return this;
    }

    @Override
    public Output writeSInt32(int si) {
        VarIntUtils.writeRawVarInt32(array, si, true);
        return this;
    }

    @Override
    public Output writeFloat(float f) {
        BytesUtils.writeFloatLE(array, f);
        return this;
    }

    @Override
    public Output writeInt64(long l) {
        VarIntUtils.writeRawVarInt64(array, l);
        return this;
    }

    @Override
    public Output writeSInt64(long sl) {
        VarIntUtils.writeRawVarInt64(array, sl, true);
        return this;
    }

    @Override
    public Output writeDouble(double d) {
        BytesUtils.writeDoubleLE(array, d);
        return this;
    }

    @Override
    public Output writeString(String s) {
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

        // UTF-8 byte length of the string is at least its UTF-16 code unit length (value.length()),
        // and at most 3 times of it. We take advantage of this in both branches below.
        //????????????
        int minLength = s.length();
        //????????????, utf8, ??????String.length()???3???
        int maxLength = minLength * UnsafeUtf8Util.MAX_BYTES_PER_CHAR;
        //????????????, bytes??????
        int minLengthVarIntSize = VarIntUtils.computeRawVarInt32Size(minLength);
        //????????????, bytes??????
        int maxLengthVarIntSize = VarIntUtils.computeRawVarInt32Size(maxLength);
        if (minLengthVarIntSize == maxLengthVarIntSize) {
            //?????????????????????????????????
            //??????buffer position
            int writerIndex = array.writerIndex();

            // Save the current position and increment past the length field. We'll come back
            // and write the length field after the encoding is complete.
            //??????????????????, ?????????????????????, ??????????????????????????????
            int startWriterIndex = writerIndex + maxLengthVarIntSize;
            array.ensureWritableBytes(maxLengthVarIntSize + maxLength);
            array.writerIndex(startWriterIndex);

            writeUFT8(s);
            int length = array.writerIndex() - writerIndex - 1;
            //??????????????????
            array.writerIndex(writerIndex);
            writeInt32(length);
            //pos??????buffer??????
            array.writerIndex(startWriterIndex + length);
        } else {
            //????????????????????????????????????
            // Calculate and write the encoded length.
            int length = UnsafeUtf8Util.encodedLength(s);
            //???????????????
            writeInt32(length);

            writeUFT8(s);
        }

        return this;
    }

    /**
     * ???utf8 string
     */
    private void writeUFT8(String s){
        int len = s.length();
        int i = 0;
        char c;
        do
        {
            c = s.charAt(i++);
            if (c < 0x80)
            {
                // ascii
                array.writeByte((byte) c);
            }
            else if (c < 0x800)
            {
                array.writeByte((byte) (0xC0 | ((c >> 6) & 0x1F)));
                array.writeByte((byte) (0x80 | ((c >> 0) & 0x3F)));
            }
            else if (Character.isHighSurrogate( c) && i < len && Character.isLowSurrogate( s.charAt(i)))
            {

                int codePoint = Character.toCodePoint( c, s.charAt(i));
                array.writeByte((byte) (0xF0 | ((codePoint >> 18) & 0x07)));
                array.writeByte((byte) (0x80 | ((codePoint >> 12) & 0x3F)));
                array.writeByte((byte) (0x80 | ((codePoint >> 6) & 0x3F)));
                array.writeByte((byte) (0x80 | ((codePoint >> 0) & 0x3F)));

                i++;
            }
            else
            {
                array.writeByte((byte) (0xE0 | ((c >> 12) & 0x0F)));
                array.writeByte((byte) (0x80 | ((c >> 6) & 0x3F)));
                array.writeByte((byte) (0x80 | ((c >> 0) & 0x3F)));
            }
        } while (i < len);
    }

    /**
     * ?????????????????????
     */
    public byte[] toByteArray() {
        return array.toByteArray();
    }

    /**
     * clear??????????????????, ???reset??????index
     */
    public void clear(){
        array.clear();
    }
}
