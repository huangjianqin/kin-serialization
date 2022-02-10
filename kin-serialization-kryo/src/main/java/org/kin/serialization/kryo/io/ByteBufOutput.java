package org.kin.serialization.kryo.io;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import org.kin.transport.netty.utils.ByteBufUtils;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * 支持自动针对{@link ByteBuf}底层映射的{@link ByteBuffer}扩容的{@link ByteBufferOutput}事项
 * <p>
 * Forked from <a href="https://github.com/fengjiachun/Jupiter">Jupiter</a>.
 *
 * @author huangjianqin
 * @date 2021/11/28
 */
public final class ByteBufOutput extends ByteBufferOutput {
    private final ByteBuf outputBuf;

    ByteBufOutput(ByteBuf outputBuf, int minWritableBytes, int maxCapacity) {
        Preconditions.checkNotNull(outputBuf);
        this.outputBuf = outputBuf;
        this.maxCapacity = maxCapacity;
        byteBuffer = ByteBufUtils.nioBuffer(outputBuf, byteBuffer, minWritableBytes);
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
            //double
            capacity = Math.min(capacity << 1, maxCapacity);
            if (capacity < 0) {
                //overflow
                capacity = maxCapacity;
            }
        }

        int minWritableBytes = capacity - position;
        byteBuffer = ByteBufUtils.nioBuffer(outputBuf, byteBuffer, minWritableBytes);
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
        ByteBufUtils.fixByteBufWriteIndex(outputBuf, byteBuffer);
    }
}
