package org.kin.serialization.protobuf.io;

import io.netty.buffer.ByteBuf;
import org.kin.framework.utils.UnsafeDirectBufferUtil;
import org.kin.framework.utils.UnsafeUtf8Util;
import org.kin.framework.utils.UnsafeUtil;
import org.kin.framework.utils.VarIntUtils;

import java.io.IOException;

import static io.protostuff.WireFormat.WIRETYPE_LENGTH_DELIMITED;
import static io.protostuff.WireFormat.makeTag;

/**
 * Forked from <a href="https://github.com/fengjiachun/Jupiter">Jupiter</a>.
 *
 * @author huangjianqin
 * @date 2021/11/28
 */
final class UnsafeNioBufOutput extends NioBufOutput {

    /**
     * Start address of the memory buffer The memory buffer should be non-movable, which normally means that is allocated
     * off-heap
     */
    private long memoryAddress;

    UnsafeNioBufOutput(ByteBuf outputBuf, int minWritableBytes, int maxCapacity) {
        super(outputBuf, minWritableBytes, maxCapacity);
        updateBufferAddress();
    }

    @Override
    protected void writeVarInt32(int value) throws IOException {
        ensureCapacity(5);
        int position = nioBuffer.position();
        if ((value & (~0 << 7)) == 0) {
            // size == 1
            UnsafeDirectBufferUtil.setByte(address(position++), (byte) value);
        } else if ((value & (~0 << 14)) == 0) {
            // size == 2
            UnsafeDirectBufferUtil.setShort(address(position),
                    (((value & 0x7F) | 0x80) << 8) | (value >>> 7));
            position += 2;
        } else if ((value & (~0 << 21)) == 0) {
            // size == 3
            UnsafeDirectBufferUtil.setShort(address(position),
                    (((value & 0x7F) | 0x80) << 8) | ((value >>> 7 & 0x7F) | 0x80));
            position += 2;
            UnsafeDirectBufferUtil.setByte(address(position++), (byte) (value >>> 14));
        } else if ((value & (~0 << 28)) == 0) {
            // size == 4
            UnsafeDirectBufferUtil.setInt(address(position),
                    (((value & 0x7F) | 0x80) << 24)
                            | (((value >>> 7 & 0x7F) | 0x80) << 16)
                            | (((value >>> 14 & 0x7F) | 0x80) << 8)
                            | (value >>> 21));
            position += 4;
        } else {
            // size == 5
            UnsafeDirectBufferUtil.setInt(address(position),
                    (((value & 0x7F) | 0x80) << 24)
                            | (((value >>> 7 & 0x7F) | 0x80) << 16)
                            | (((value >>> 14 & 0x7F) | 0x80) << 8)
                            | ((value >>> 21 & 0x7F) | 0x80));
            position += 4;
            UnsafeDirectBufferUtil.setByte(address(position++), (byte) (value >>> 28));
        }
        nioBuffer.position(position);
    }

    @SuppressWarnings("DuplicateExpressions")
    @Override
    protected void writeVarInt64(long value) throws IOException {
        ensureCapacity(10);
        int position = nioBuffer.position();
        // Handle two popular special cases up front ...
        if ((value & (~0L << 7)) == 0) {
            // size == 1
            UnsafeDirectBufferUtil.setByte(address(position++), (byte) value);
        } else if (value < 0L) {
            // size == 10
            UnsafeDirectBufferUtil.setLong(address(position),
                    (((value & 0x7F) | 0x80) << 56)
                            | (((value >>> 7 & 0x7F) | 0x80) << 48)
                            | (((value >>> 14 & 0x7F) | 0x80) << 40)
                            | (((value >>> 21 & 0x7F) | 0x80) << 32)
                            | (((value >>> 28 & 0x7F) | 0x80) << 24)
                            | (((value >>> 35 & 0x7F) | 0x80) << 16)
                            | (((value >>> 42 & 0x7F) | 0x80) << 8)
                            | ((value >>> 49 & 0x7F) | 0x80));
            position += 8;
            UnsafeDirectBufferUtil.setShort(address(position),
                    ((((int) (value >>> 56) & 0x7F) | 0x80) << 8) | (int) (value >>> 63));
            position += 2;
        }
        // ... leaving us with 8 remaining [2, 3, 4, 5, 6, 7, 8, 9]
        else if ((value & (~0L << 14)) == 0) {
            // size == 2
            UnsafeDirectBufferUtil.setShort(address(position),
                    ((((int) value & 0x7F) | 0x80) << 8) | (byte) (value >>> 7));
            position += 2;
        } else if ((value & (~0L << 21)) == 0) {
            // size == 3
            UnsafeDirectBufferUtil.setShort(address(position),
                    ((((int) value & 0x7F) | 0x80) << 8) | (((int) value >>> 7 & 0x7F) | 0x80));
            position += 2;
            UnsafeDirectBufferUtil.setByte(address(position++), (byte) (value >>> 14));
        } else if ((value & (~0L << 28)) == 0) {
            // size == 4
            UnsafeDirectBufferUtil.setInt(address(position),
                    ((((int) value & 0x7F) | 0x80) << 24)
                            | ((((int) value >>> 7 & 0x7F) | 0x80) << 16)
                            | ((((int) value >>> 14 & 0x7F) | 0x80) << 8)
                            | ((int) (value >>> 21)));
            position += 4;
        } else if ((value & (~0L << 35)) == 0) {
            // size == 5
            UnsafeDirectBufferUtil.setInt(address(position),
                    ((((int) value & 0x7F) | 0x80) << 24)
                            | ((((int) value >>> 7 & 0x7F) | 0x80) << 16)
                            | ((((int) value >>> 14 & 0x7F) | 0x80) << 8)
                            | (((int) value >>> 21 & 0x7F) | 0x80));
            position += 4;
            UnsafeDirectBufferUtil.setByte(address(position++), (byte) (value >>> 28));
        } else if ((value & (~0L << 42)) == 0) {
            // size == 6
            UnsafeDirectBufferUtil.setInt(address(position),
                    ((((int) value & 0x7F) | 0x80) << 24)
                            | ((((int) value >>> 7 & 0x7F) | 0x80) << 16)
                            | ((((int) value >>> 14 & 0x7F) | 0x80) << 8)
                            | (((int) value >>> 21 & 0x7F) | 0x80)
            );
            position += 4;
            UnsafeDirectBufferUtil.setShort(address(position),
                    ((((int) (value >>> 28) & 0x7F) | 0x80) << 8) | (int) (value >>> 35));
            position += 2;
        } else if ((value & (~0L << 49)) == 0) {
            // size == 7
            UnsafeDirectBufferUtil.setInt(address(position),
                    ((((int) value & 0x7F) | 0x80) << 24)
                            | ((((int) value >>> 7 & 0x7F) | 0x80) << 16)
                            | ((((int) value >>> 14 & 0x7F) | 0x80) << 8)
                            | (((int) value >>> 21 & 0x7F) | 0x80)
            );
            position += 4;
            UnsafeDirectBufferUtil.setShort(address(position),
                    ((((int) (value >>> 28) & 0x7F) | 0x80) << 8) | (((int) (value >>> 35) & 0x7F) | 0x80));
            position += 2;
            UnsafeDirectBufferUtil.setByte(address(position++), (byte) (value >>> 42));
        } else if ((value & (~0L << 56)) == 0) {
            // size == 8
            UnsafeDirectBufferUtil.setLong(address(position),
                    (((value & 0x7F) | 0x80) << 56)
                            | (((value >>> 7 & 0x7F) | 0x80) << 48)
                            | (((value >>> 14 & 0x7F) | 0x80) << 40)
                            | (((value >>> 21 & 0x7F) | 0x80) << 32)
                            | (((value >>> 28 & 0x7F) | 0x80) << 24)
                            | (((value >>> 35 & 0x7F) | 0x80) << 16)
                            | (((value >>> 42 & 0x7F) | 0x80) << 8)
                            | (value >>> 49));
            position += 8;
        } else {
            // size == 9 (value & (~0L << 63)) == 0
            UnsafeDirectBufferUtil.setLong(address(position),
                    (((value & 0x7F) | 0x80) << 56)
                            | (((value >>> 7 & 0x7F) | 0x80) << 48)
                            | (((value >>> 14 & 0x7F) | 0x80) << 40)
                            | (((value >>> 21 & 0x7F) | 0x80) << 32)
                            | (((value >>> 28 & 0x7F) | 0x80) << 24)
                            | (((value >>> 35 & 0x7F) | 0x80) << 16)
                            | (((value >>> 42 & 0x7F) | 0x80) << 8)
                            | ((value >>> 49 & 0x7F) | 0x80));
            position += 8;
            UnsafeDirectBufferUtil.setByte(address(position++), (byte) (value >>> 56));
        }
        nioBuffer.position(position);
    }

    @Override
    protected void writeInt32LE(int value) throws IOException {
        ensureCapacity(4);
        int position = nioBuffer.position();
        UnsafeDirectBufferUtil.setIntLE(address(position), value);
        nioBuffer.position(position + 4);
    }

    @Override
    protected void writeInt64LE(long value) throws IOException {
        ensureCapacity(8);
        int position = nioBuffer.position();
        UnsafeDirectBufferUtil.setLongLE(address(position), value);
        nioBuffer.position(position + 8);
    }

    @Override
    protected void writeByte(byte value) throws IOException {
        ensureCapacity(1);
        int position = nioBuffer.position();
        UnsafeDirectBufferUtil.setByte(address(position), value);
        nioBuffer.position(position + 1);
    }

    @Override
    protected void writeByteArray(byte[] value, int offset, int length) throws IOException {
        ensureCapacity(length);
        int position = nioBuffer.position();
        UnsafeDirectBufferUtil.setBytes(address(position), value, offset, length);
        nioBuffer.position(position + length);
    }

    @Override
    protected void ensureCapacity(int required) throws ProtocolException {
        if (nioBuffer.remaining() < required) {
            int position = nioBuffer.position();

            while (capacity - position < required) {
                if (capacity == maxCapacity) {
                    throw new ProtocolException(
                            "Buffer overflow. Available: " + (capacity - position) + ", required: " + required);
                }
                capacity = Math.min(capacity << 1, maxCapacity);
                if (capacity < 0) {
                    capacity = maxCapacity;
                }
            }

            nioBuffer = NioBufOutput.nioByteBuffer(outputBuf, nioBuffer, capacity - position);
            capacity = nioBuffer.limit();
            // Need to update the direct buffer's memory address
            updateBufferAddress();
        }
    }

    private void updateBufferAddress() {
        memoryAddress = UnsafeUtil.addressOffset(nioBuffer);
    }

    private long address(int position) {
        return memoryAddress + position;
    }
}