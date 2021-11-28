package org.kin.serialization.kryo.io;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import io.netty.buffer.ByteBuf;
import org.kin.framework.io.ByteBufferUtils;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Forked from <a href="https://github.com/fengjiachun/Jupiter">Jupiter</a>.
 *
 * @author huangjianqin
 * @date 2021/11/28
 */
public final class NioBufOutput extends ByteBufferOutput {
    /** 解析netty{@link ByteBuf}才需要赋值 */
    private final ByteBuf outputBuf;

    NioBufOutput(ByteBuf outputBuf, int minWritableBytes, int maxCapacity) {
        this.outputBuf = outputBuf;
        this.maxCapacity = maxCapacity;
        byteBuffer = nioByteBuffer(outputBuf, byteBuffer, minWritableBytes);
        capacity = byteBuffer.remaining();
    }

    NioBufOutput(ByteBuffer byteBuffer, int maxCapacity) {
        this.outputBuf = null;
        this.maxCapacity = maxCapacity;
        this.byteBuffer = byteBuffer;
        capacity = byteBuffer.remaining();
    }

    @Override
    protected boolean require(int required) throws KryoException {
        if (capacity - position >= required) {
            return false;
        }
        if (required > maxCapacity) {
            throw new KryoException("Buffer overflow. Max capacity: " + maxCapacity + ", required: " + required);
        }

        flush();

        while (capacity - position < required) {
            if (capacity == maxCapacity) {
                throw new KryoException("Buffer overflow. Available: " + (capacity - position) + ", required: " + required);
            }
            // Grow buffer.
            if (capacity == 0) {
                capacity = 1;
            }
            capacity = Math.min(capacity << 1, maxCapacity);
            if (capacity < 0) {
                capacity = maxCapacity;
            }
        }

        int minWritableBytes = capacity - position;
        byteBuffer = Objects.nonNull(outputBuf) ? nioByteBuffer(outputBuf, byteBuffer, minWritableBytes) :
                ByteBufferUtils.ensureWritableBytes(byteBuffer, minWritableBytes);
        capacity = byteBuffer.limit();
        return true;
    }

    /**
     * 修正{@link ByteBuf#writerIndex()}
     * 因为底层使用{@link ByteBuf#nioBuffer()}获取{@link java.nio.ByteBuffer}实例,
     * 但修改{@link java.nio.ByteBuffer}实例, 对{@link io.netty.buffer.ByteBuf}不可见,
     * 故完成output后需要修正{@link ByteBuf#writerIndex()}
     */
    public void fixByteBufWriteIndex() {
        if (Objects.isNull(outputBuf)) {
            throw new UnsupportedOperationException("outputBuf is null, so this method is not supported");
        }
        int actualWroteBytes = outputBuf.writerIndex();
        if (byteBuffer != null) {
            actualWroteBytes += byteBuffer.position();
        }

        outputBuf.writerIndex(actualWroteBytes);
    }

    /**
     * 创建{@link ByteBuf}内存映射的满足最小可写字节数{@code minWritableBytes}的{@link ByteBuffer}实例
     */
    static ByteBuffer nioByteBuffer(ByteBuf byteBuf, ByteBuffer byteBuffer, int minWritableBytes) {
        if (minWritableBytes < 0) {
            minWritableBytes = byteBuf.writableBytes();
        }

        if (byteBuffer == null) {
            byteBuffer = newNioByteBuffer(byteBuf, minWritableBytes);
        }

        if (byteBuffer.remaining() >= minWritableBytes) {
            return byteBuffer;
        }

        int position = byteBuffer.position();
        byteBuffer = newNioByteBuffer(byteBuf, position + minWritableBytes);
        byteBuffer.position(position);
        return byteBuffer;
    }

    static ByteBuffer newNioByteBuffer(ByteBuf byteBuf, int writableBytes) {
        return byteBuf
                .ensureWritable(writableBytes)
                .nioBuffer(byteBuf.writerIndex(), byteBuf.writableBytes());
    }
}
