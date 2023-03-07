package org.kin.kinbuffer.io;

import io.netty.buffer.ByteBuf;
import org.kin.framework.io.UnsafeByteBufferOutput;
import org.kin.framework.utils.BytesUtils;
import org.kin.framework.utils.VarIntUtils;

import java.nio.ByteBuffer;

/**
 * 基于unsafe, 适用于direct bytebuffer和direct bytebuf
 * @author huangjianqin
 * @date 2022/4/16
 */
public final class UnsafeNioBufOutput extends NioBufOutput {
    private UnsafeByteBufferOutput output;

    UnsafeNioBufOutput(ByteBuf byteBuffer) {
        super(byteBuffer);
        output = new UnsafeByteBufferOutput(getByteBuffer());
    }

    UnsafeNioBufOutput(ByteBuffer byteBuffer) {
        super(byteBuffer);
        output = new UnsafeByteBufferOutput(byteBuffer);
    }

    @Override
    protected boolean ensureCapacity(int required) {
        boolean ret = super.ensureCapacity(required);
        if (ret) {
            output = new UnsafeByteBufferOutput(getByteBuffer());
        }

        return ret;
    }

    @Override
    public Output writeBoolean(boolean bool) {
        ensureCapacity(1);
        output.writeByte(bool ? 1 : 0);
        return this;
    }

    @Override
    public Output writeBytes(byte[] bytes) {
        ensureCapacity(bytes.length);
        output.writeBytes(bytes);
        return this;
    }

    @Override
    public Output writeByte(int b) {
        ensureCapacity(1);
        output.writeByte(b);
        return this;
    }

    @Override
    public Output writeVarInt32(int i) {
        ensureCapacity(5);
        VarIntUtils.writeRawVarInt32(output, i);
        return this;
    }

    @Override
    public Output writeSVarInt32(int si) {
        ensureCapacity(5);
        VarIntUtils.writeRawVarInt32(output, si, true);
        return this;
    }

    @Override
    public Output writeFloat(float f) {
        ensureCapacity(4);
        BytesUtils.writeFloatLE(output, f);
        return this;
    }

    @Override
    public Output writeVarInt64(long l) {
        ensureCapacity(10);
        VarIntUtils.writeRawVarInt64(output, l);
        return this;
    }

    @Override
    public Output writeSVarInt64(long sl) {
        ensureCapacity(10);
        VarIntUtils.writeRawVarInt64(output, sl, true);
        return this;
    }

    @Override
    public Output writeDouble(double d) {
        ensureCapacity(8);
        BytesUtils.writeDoubleLE(output, d);
        return this;
    }
}
