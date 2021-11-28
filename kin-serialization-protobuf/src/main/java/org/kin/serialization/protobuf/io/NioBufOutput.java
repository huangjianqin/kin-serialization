package org.kin.serialization.protobuf.io;

import io.netty.buffer.ByteBuf;
import io.protostuff.*;
import org.kin.framework.utils.UnsafeUtf8Util;
import org.kin.framework.utils.UnsafeUtil;
import org.kin.framework.utils.VarIntUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

import static io.protostuff.ProtobufOutput.encodeZigZag32;
import static io.protostuff.ProtobufOutput.encodeZigZag64;
import static io.protostuff.WireFormat.*;

/**
 * Forked from <a href="https://github.com/fengjiachun/Jupiter">Jupiter</a>.
 *
 * @author huangjianqin
 * @date 2021/11/28
 */
class NioBufOutput implements Output {
    protected final ByteBuf outputBuf;
    protected final int maxCapacity;
    protected ByteBuffer nioBuffer;
    protected int capacity;

    NioBufOutput(ByteBuf outputBuf, int minWritableBytes, int maxCapacity) {
        this.outputBuf = outputBuf;
        this.maxCapacity = maxCapacity;
        nioBuffer = nioByteBuffer(outputBuf, null, minWritableBytes);
        capacity = nioBuffer.remaining();
    }

    @Override
    public void writeInt32(int fieldNumber, int value, boolean repeated) throws IOException {
        if (value < 0) {
            writeVarInt32(makeTag(fieldNumber, WIRETYPE_VARINT));
            writeVarInt64(value);
        } else {
            writeVarInt32(makeTag(fieldNumber, WIRETYPE_VARINT));
            writeVarInt32(value);
        }
    }

    @Override
    public void writeUInt32(int fieldNumber, int value, boolean repeated) throws IOException {
        writeVarInt32(makeTag(fieldNumber, WIRETYPE_VARINT));
        writeVarInt32(value);
    }

    @Override
    public void writeSInt32(int fieldNumber, int value, boolean repeated) throws IOException {
        writeVarInt32(makeTag(fieldNumber, WIRETYPE_VARINT));
        writeVarInt32(encodeZigZag32(value));
    }

    @Override
    public void writeFixed32(int fieldNumber, int value, boolean repeated) throws IOException {
        writeVarInt32(makeTag(fieldNumber, WIRETYPE_FIXED32));
        writeInt32LE(value);
    }

    @Override
    public void writeSFixed32(int fieldNumber, int value, boolean repeated) throws IOException {
        writeVarInt32(makeTag(fieldNumber, WIRETYPE_FIXED32));
        writeInt32LE(value);
    }

    @Override
    public void writeInt64(int fieldNumber, long value, boolean repeated) throws IOException {
        writeVarInt32(makeTag(fieldNumber, WIRETYPE_VARINT));
        writeVarInt64(value);
    }

    @Override
    public void writeUInt64(int fieldNumber, long value, boolean repeated) throws IOException {
        writeVarInt32(makeTag(fieldNumber, WIRETYPE_VARINT));
        writeVarInt64(value);
    }

    @Override
    public void writeSInt64(int fieldNumber, long value, boolean repeated) throws IOException {
        writeVarInt32(makeTag(fieldNumber, WIRETYPE_VARINT));
        writeVarInt64(encodeZigZag64(value));
    }

    @Override
    public void writeFixed64(int fieldNumber, long value, boolean repeated) throws IOException {
        writeVarInt32(makeTag(fieldNumber, WIRETYPE_FIXED64));
        writeInt64LE(value);
    }

    @Override
    public void writeSFixed64(int fieldNumber, long value, boolean repeated) throws IOException {
        writeVarInt32(makeTag(fieldNumber, WIRETYPE_FIXED64));
        writeInt64LE(value);
    }

    @Override
    public void writeFloat(int fieldNumber, float value, boolean repeated) throws IOException {
        writeVarInt32(makeTag(fieldNumber, WIRETYPE_FIXED32));
        writeInt32LE(Float.floatToRawIntBits(value));
    }

    @Override
    public void writeDouble(int fieldNumber, double value, boolean repeated) throws IOException {
        writeVarInt32(makeTag(fieldNumber, WIRETYPE_FIXED64));
        writeInt64LE(Double.doubleToRawLongBits(value));
    }

    @Override
    public void writeBool(int fieldNumber, boolean value, boolean repeated) throws IOException {
        writeVarInt32(makeTag(fieldNumber, WIRETYPE_VARINT));
        writeByte(value ? (byte) 0x01 : 0x00);
    }

    @Override
    public void writeEnum(int fieldNumber, int value, boolean repeated) throws IOException {
        writeInt32(fieldNumber, value, repeated);
    }

    @Override
    public void writeString(int fieldNumber, CharSequence value, boolean repeated) throws IOException {
        if (!UnsafeUtil.hasUnsafe()) {
            writeByteArray(fieldNumber, StringSerializer.STRING.ser(value.toString()), repeated);
            return;
        }

        writeVarInt32(makeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED));

        // UTF-8 byte length of the string is at least its UTF-16 code unit length (value.length()),
        // and at most 3 times of it. We take advantage of this in both branches below.
        int minLength = value.length();
        int maxLength = minLength * UnsafeUtf8Util.MAX_BYTES_PER_CHAR;
        int minLengthVarIntSize = VarIntUtils.computeRawVarInt32Size(minLength);
        int maxLengthVarIntSize = VarIntUtils.computeRawVarInt32Size(maxLength);
        if (minLengthVarIntSize == maxLengthVarIntSize) {
            int position = nioBuffer.position();

            ensureCapacity(maxLengthVarIntSize + maxLength);

            // Save the current position and increment past the length field. We'll come back
            // and write the length field after the encoding is complete.
            int stringStartPos = position + maxLengthVarIntSize;
            nioBuffer.position(stringStartPos);

            int length;
            // Encode the string.
            if (nioBuffer.isDirect()) {
                UnsafeUtf8Util.encodeUtf8Direct(value, nioBuffer);
                // Write the length and advance the position.
                length = nioBuffer.position() - stringStartPos;
            } else {
                int offset = nioBuffer.arrayOffset() + stringStartPos;
                int outIndex = UnsafeUtf8Util.encodeUtf8(value, nioBuffer.array(), offset, nioBuffer.remaining());
                length = outIndex - offset;
            }
            nioBuffer.position(position);
            writeVarInt32(length);
            nioBuffer.position(stringStartPos + length);
        } else {
            // Calculate and write the encoded length.
            int length = UnsafeUtf8Util.encodedLength(value);
            writeVarInt32(length);

            ensureCapacity(length);

            if (nioBuffer.isDirect()) {
                // Write the string and advance the position.
                UnsafeUtf8Util.encodeUtf8Direct(value, nioBuffer);
            } else {
                int pos = nioBuffer.position();
                UnsafeUtf8Util.encodeUtf8(value, nioBuffer.array(), nioBuffer.arrayOffset() + pos, nioBuffer.remaining());
                nioBuffer.position(pos + length);
            }
        }
    }

    @Override
    public void writeBytes(int fieldNumber, ByteString value, boolean repeated) throws IOException {
        writeByteArray(fieldNumber, ZeroByteStringHelper.getBytes(value), repeated);
    }

    @Override
    public void writeByteArray(int fieldNumber, byte[] value, boolean repeated) throws IOException {
        writeByteRange(false, fieldNumber, value, 0, value.length, repeated);
    }

    @Override
    public void writeByteRange(boolean utf8String, int fieldNumber, byte[] value, int offset, int length, boolean repeated) throws IOException {
        writeVarInt32(makeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED));
        writeVarInt32(length);
        writeByteArray(value, offset, length);
    }

    @Override
    public <T> void writeObject(int fieldNumber, T value, Schema<T> schema, boolean repeated) throws IOException {
        writeVarInt32(makeTag(fieldNumber, WIRETYPE_START_GROUP));
        schema.writeTo(this, value);
        writeVarInt32(makeTag(fieldNumber, WIRETYPE_END_GROUP));
    }

    @Override
    public void writeBytes(int fieldNumber, ByteBuffer value, boolean repeated) throws IOException {
        writeByteRange(false, fieldNumber, value.array(), value.arrayOffset() + value.position(),
                value.remaining(), repeated);
    }

    // TODO: 2021/11/28 上zigzag
    protected void writeVarInt32(int value) throws IOException {
        ensureCapacity(5);
        while (true) {
            if ((value & ~0x7F) == 0) {
                nioBuffer.put((byte) value);
                return;
            } else {
                nioBuffer.put((byte) ((value & 0x7F) | 0x80));
                value >>>= 7;
            }
        }
    }

    protected void writeVarInt64(long value) throws IOException {
        ensureCapacity(10);
        while (true) {
            if ((value & ~0x7FL) == 0) {
                nioBuffer.put((byte) value);
                return;
            } else {
                nioBuffer.put((byte) (((int) value & 0x7F) | 0x80));
                value >>>= 7;
            }
        }
    }

    protected void writeInt32LE(final int value) throws IOException {
        ensureCapacity(4);
        IntSerializer.writeInt32LE(value, nioBuffer);
    }

    protected void writeInt64LE(final long value) throws IOException {
        ensureCapacity(8);
        IntSerializer.writeInt64LE(value, nioBuffer);
    }

    protected void writeByte(final byte value) throws IOException {
        ensureCapacity(1);
        nioBuffer.put(value);
    }

    protected void writeByteArray(final byte[] value,
                                  final int offset, final int length) throws IOException {
        ensureCapacity(length);
        nioBuffer.put(value, offset, length);
    }

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

            nioBuffer = nioByteBuffer(outputBuf, nioBuffer, capacity - position);
            capacity = nioBuffer.limit();
        }
    }

    @Override
    public void fixByteBufWriteIndex() {
        int actualWroteBytes = outputBuf.writerIndex();
        if (nioBuffer != null) {
            actualWroteBytes += nioBuffer.position();
        }

        outputBuf.writerIndex(actualWroteBytes);
    }

    /**
     * 创建{@link ByteBuf}内存映射的满足最小可写字节数{@code minWritableBytes}的{@link ByteBuffer}实例
     */
    static ByteBuffer nioByteBuffer(ByteBuf byteBuf, ByteBuffer nioBuffer, int minWritableBytes) {
        if (minWritableBytes < 0) {
            minWritableBytes = byteBuf.writableBytes();
        }

        if (nioBuffer == null) {
            nioBuffer = newNioByteBuffer(byteBuf, minWritableBytes);
        }

        if (nioBuffer.remaining() >= minWritableBytes) {
            return nioBuffer;
        }

        int position = nioBuffer.position();
        nioBuffer = newNioByteBuffer(byteBuf, position + minWritableBytes);
        nioBuffer.position(position);
        return nioBuffer;
    }

    static ByteBuffer newNioByteBuffer(ByteBuf byteBuf, int writableBytes) {
        return byteBuf
                .ensureWritable(writableBytes)
                .nioBuffer(byteBuf.writerIndex(), byteBuf.writableBytes());
    }
}