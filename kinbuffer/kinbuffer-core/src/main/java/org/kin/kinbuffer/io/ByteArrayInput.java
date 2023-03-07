package org.kin.kinbuffer.io;

import org.kin.framework.utils.BytesUtils;
import org.kin.framework.utils.VarIntUtils;

/**
 * @author huangjianqin
 * @date 2022/4/15
 */
public final class ByteArrayInput implements Input{
    private InternalByteArrayInput input;

    public ByteArrayInput(byte[] buffer) {
        this.input = new InternalByteArrayInput(buffer);
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
        return BytesUtils.readInt32LE(input) & 0xFFFFFFFFL;
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
        if (len == 0) {
            return null;
        }
        else if (len == 1) {
            return "";
        }

        int readerIndex = input.readerIndex();
        input.readerIndex(readerIndex + len);
        return new String(input.buffer, readerIndex, len);
    }

    /**
     * 底层维护bytes数组的{@link Input}实现
     */
    private static final class InternalByteArrayInput implements org.kin.framework.io.Input{
        /** bytes */
        private final byte[] buffer;
        /** 当前位置 */
        private int pos;

        public InternalByteArrayInput(byte[] buffer) {
            this.buffer = buffer;
        }

        @Override
        public byte readByte() {
            return buffer[pos++];
        }

        @Override
        public org.kin.framework.io.Input readBytes(byte[] dst, int dstIndex, int length) {
            System.arraycopy(buffer, pos, dst, dstIndex, length);
            pos += length;
            return this;
        }

        @Override
        public int readableBytes() {
            return buffer.length - pos;
        }

        @Override
        public int readerIndex() {
            return pos;
        }

        @Override
        public org.kin.framework.io.Input readerIndex(int readerIndex) {
            if(readerIndex > buffer.length){
                throw new IllegalArgumentException("reader index is greater than buffer size");
            }

            pos = readerIndex;
            return this;
        }

        @Override
        public boolean readerIndexSupported() {
            return true;
        }
    }
}
