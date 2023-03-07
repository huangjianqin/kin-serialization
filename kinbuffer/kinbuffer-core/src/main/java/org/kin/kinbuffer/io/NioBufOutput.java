package org.kin.kinbuffer.io;

import io.netty.buffer.ByteBuf;
import org.kin.framework.io.ByteBufferUtils;
import org.kin.framework.utils.BytesUtils;
import org.kin.framework.utils.UnsafeUtf8Util;
import org.kin.framework.utils.UnsafeUtil;
import org.kin.framework.utils.VarIntUtils;
import org.kin.transport.netty.utils.ByteBufUtils;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2022/4/15
 */
 public class NioBufOutput implements Output{
    /** 解析netty{@link ByteBuf}才需要赋值 */
    protected ByteBuf outputBuf;
    protected ByteBuffer byteBuffer;
    protected int capacity;

    NioBufOutput(ByteBuf outputBuf) {
        this.outputBuf = outputBuf;
        byteBuffer = ByteBufUtils.nioBuffer(outputBuf, null, outputBuf.writableBytes());
        capacity = byteBuffer.remaining();
    }

    NioBufOutput(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        capacity = byteBuffer.remaining();
    }

    /**
     * 保证底层{@link ByteBuffer}拥有{@code required}可写空间
     */
    protected boolean ensureCapacity(int required) {
        if (byteBuffer.remaining() < required) {
            int position = byteBuffer.position();

            while (capacity - position < required) {
                //double
                capacity = capacity << 1;
                if (capacity < 0) {
                    //overflow
                    capacity = Integer.MAX_VALUE;
                }
            }

            int minWritableBytes = capacity - position;
            byteBuffer = Objects.nonNull(outputBuf) ? ByteBufUtils.nioBuffer(outputBuf, byteBuffer, minWritableBytes) :
                    ByteBufferUtils.ensureWritableBytes(byteBuffer, minWritableBytes);
            capacity = byteBuffer.limit();
            return true;
        }
        return false;
    }

    @Override
    public Output writeBoolean(boolean bool) {
        ensureCapacity(1);
        byteBuffer.put((byte) (bool ? 1 : 0));
        return this;
    }

    @Override
    public Output writeBytes(byte[] bytes) {
        ensureCapacity(bytes.length);
        byteBuffer.put(bytes);
        return this;
    }

    @Override
    public Output writeByte(int b) {
        ensureCapacity(1);
        byteBuffer.put((byte) b);
        return this;
    }

    @Override
    public Output writeInt32(int i) {
        ensureCapacity(4);
        BytesUtils.writeFloatLE(byteBuffer, i);
        return this;
    }

    @Override
    public Output writeUInt32(int i) {
        ensureCapacity(4);
        BytesUtils.writeFloatLE(byteBuffer, i);
        return this;
    }

    @Override
    public Output writeSInt32(int si) {
        ensureCapacity(4);
        BytesUtils.writeFloatLE(byteBuffer, si);
        return this;
    }

    @Override
    public Output writeVarInt32(int i) {
        ensureCapacity(5);
        VarIntUtils.writeRawVarInt32(byteBuffer, i);
        return this;
    }

    @Override
    public Output writeSVarInt32(int si) {
        ensureCapacity(5);
        VarIntUtils.writeRawVarInt32(byteBuffer, si, true);
        return this;
    }

    @Override
    public Output writeFloat(float f) {
        ensureCapacity(4);
        BytesUtils.writeFloatLE(byteBuffer, f);
        return this;
    }

    @Override
    public Output writeInt64(long l) {
        ensureCapacity(10);
        BytesUtils.writeInt64LE(byteBuffer, l);
        return this;
    }

    @Override
    public Output writeSInt64(long sl) {
        ensureCapacity(10);
        BytesUtils.writeInt64LE(byteBuffer, sl);
        return this;
    }

    @Override
    public Output writeVarInt64(long l) {
        ensureCapacity(10);
        VarIntUtils.writeRawVarInt64(byteBuffer, l);
        return this;
    }

    @Override
    public Output writeSVarInt64(long sl) {
        ensureCapacity(10);
        VarIntUtils.writeRawVarInt64(byteBuffer, sl, true);
        return this;
    }

    @Override
    public Output writeDouble(double d) {
        ensureCapacity(8);
        BytesUtils.writeDoubleLE(byteBuffer, d);
        return this;
    }

    @Override
    public Output writeString(String s) {
        if (!UnsafeUtil.hasUnsafe()) {
            return Output.super.writeString(s);
        }

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

        // UTF-8 byte length of the string is at least its UTF-16 code unit length (value.length()),
        // and at most 3 times of it. We take advantage of this in both branches below.
        //最少长度
        int minLength = s.length();
        //最大长度, utf8, 最多String.length()的3倍
        int maxLength = minLength * UnsafeUtf8Util.MAX_BYTES_PER_CHAR;
        //最少长度, bytes长度
        int minLengthVarIntSize = VarIntUtils.computeRawVarInt32Size(minLength);
        //最大长度, bytes长度
        int maxLengthVarIntSize = VarIntUtils.computeRawVarInt32Size(maxLength);
        if (minLengthVarIntSize == maxLengthVarIntSize) {
            //最少长度与最大长度一样
            //当前buffer position
            int position = byteBuffer.position();
            //保证buffer有足够的可写空间
            ensureCapacity(maxLengthVarIntSize + maxLength);

            // Save the current position and increment past the length field. We'll come back
            // and write the length field after the encoding is complete.
            //跳过字符长度, 先写字符串内容, 后面再把字符长度写入
            int stringStartPos = position + maxLengthVarIntSize;
            byteBuffer.position(stringStartPos);

            int length;
            // Encode the string.
            if (byteBuffer.isDirect()) {
                //direct
                UnsafeUtf8Util.encodeUtf8Direct(s, byteBuffer);
                // Write the length and advance the position.
                //字符串实际长度
                length = byteBuffer.position() - stringStartPos;
            } else {
                //heap
                int offset = byteBuffer.arrayOffset() + stringStartPos;
                int outIndex = UnsafeUtf8Util.encodeUtf8(s, byteBuffer.array(), offset, byteBuffer.remaining());
                //字符串实际长度
                length = outIndex - offset;
            }
            //回写字符长度
            byteBuffer.position(position);
            writeVarInt32(length);
            //pos回到buffer末尾
            byteBuffer.position(stringStartPos + length);
        } else {
            //最少长度与最大长度不一样
            // Calculate and write the encoded length.
            int length = UnsafeUtf8Util.encodedLength(s);
            //写字符长度
            writeVarInt32(length);

            ensureCapacity(length);
            if (byteBuffer.isDirect()) {
                //direct
                // Write the string and advance the position.
                UnsafeUtf8Util.encodeUtf8Direct(s, byteBuffer);
            } else {
                //heap
                int pos = byteBuffer.position();
                UnsafeUtf8Util.encodeUtf8(s, byteBuffer.array(), byteBuffer.arrayOffset() + pos, byteBuffer.remaining());
                byteBuffer.position(pos + length);
            }
        }
        return this;
    }

    @Override
    public final void fixWriteIndex() {
        if (Objects.isNull(outputBuf)) {
            throw new UnsupportedOperationException("outputBuf is null, so this method is not supported");
        }
        ByteBufUtils.fixByteBufWriteIndex(outputBuf, byteBuffer);
    }

    /**
     * 获取底层的{@link ByteBuffer}
     */
    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }
}
