package org.kin.kinbuffer.io;

import io.netty.buffer.ByteBuf;
import org.kin.framework.io.UnsafeByteBufferOutput;
import org.kin.framework.utils.BytesUtils;
import org.kin.framework.utils.UnsafeUtf8Util;
import org.kin.framework.utils.VarIntUtils;

import java.nio.ByteBuffer;

/**
 * 基于unsafe, 适用于direct bytebuffer
 * @author huangjianqin
 * @date 2022/4/15
 */
public final class UnsafeNioBufInput implements Input{
    private final ByteBuffer byteBuffer;
    private final org.kin.framework.io.UnsafeByteBufferInput input;

    public UnsafeNioBufInput(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        this.input = new org.kin.framework.io.UnsafeByteBufferInput(byteBuffer);
    }

    public UnsafeNioBufInput(ByteBuf byteBuf) {
        this(byteBuf.nioBuffer());
    }

    @Override
    public boolean readBoolean() {
        return input.readByte() != 0;
    }

    @Override
    public byte[] readBytes(int len) {
        byte[] ret = new byte[len];
        input.readBytes(ret);
        return ret;
    }

    @Override
    public byte[] readBytes() {
        return readBytes(input.readableBytes());
    }

    @Override
    public byte readByte() {
        return input.readByte();
    }

    @Override
    public int readInt32() {
        return BytesUtils.readInt32LE(input);
    }

    @Override
    public long readUInt32() {
        return BytesUtils.readInt32LE(input)& 0xFFFFFFFFL;
    }

    @Override
    public int readSInt32() {
        return BytesUtils.readInt32LE(input);
    }

    @Override
    public int readVarInt32() {
        return VarIntUtils.readRawVarInt32(input);
    }

    @Override
    public int readSVarInt32() {
        return VarIntUtils.readRawVarInt32(input, true);
    }

    @Override
    public float readFloat() {
        return BytesUtils.readFloatLE(input);
    }

    @Override
    public long readInt64() {
        return BytesUtils.readInt64LE(input);
    }

    @Override
    public long readSInt64() {
        return BytesUtils.readInt64LE(input);
    }

    @Override
    public long readVarInt64() {
        return VarIntUtils.readRawVarInt64(input);
    }

    @Override
    public long readSVarInt64() {
        return VarIntUtils.readRawVarInt64(input, true);
    }

    @Override
    public double readDouble() {
        return BytesUtils.readDoubleLE(input);
    }

    @Override
    public String readString(int len) {
        //direct bytebuffer
        if (len == 0) {
            return null;
        }
        else if (len == 1) {
            return "";
        }

        int position = byteBuffer.position();
        String result = UnsafeUtf8Util.decodeUtf8Direct(byteBuffer, position, len);
        byteBuffer.position(position + len);
        return result;
    }
}
