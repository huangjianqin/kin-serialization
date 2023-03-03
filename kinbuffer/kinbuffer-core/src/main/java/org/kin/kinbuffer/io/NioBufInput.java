package org.kin.kinbuffer.io;

import io.netty.buffer.ByteBuf;
import org.kin.framework.io.ByteBufferUtils;
import org.kin.framework.utils.BytesUtils;
import org.kin.framework.utils.UnsafeUtf8Util;
import org.kin.framework.utils.UnsafeUtil;
import org.kin.framework.utils.VarIntUtils;
import org.kin.transport.netty.utils.ByteBufUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2022/4/15
 */
 public class NioBufInput implements Input{
    protected ByteBuf inputBuf;
    protected final ByteBuffer byteBuffer;

    public NioBufInput(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public NioBufInput(ByteBuf byteBuf) {
        this.inputBuf = byteBuf;
        this.byteBuffer = this.inputBuf.nioBuffer();
    }

    @Override
    public boolean readBoolean() {
        return byteBuffer.get() != 0;
    }

    @Override
    public byte[] readBytes(int len) {
        byte[] ret = new byte[len];
        byteBuffer.get(ret, 0, len);
        return ret;
    }

    @Override
    public byte[] readBytes() {
        return readBytes(ByteBufferUtils.getReadableBytes(byteBuffer));
    }

    @Override
    public byte readByte() {
        return byteBuffer.get();
    }

    @Override
    public int readInt32() {
        return VarIntUtils.readRawVarInt32(byteBuffer);
    }

    @Override
    public int readSInt32() {
        return VarIntUtils.readRawVarInt32(byteBuffer, true);
    }

    @Override
    public float readFloat() {
        return BytesUtils.readFloatLE(byteBuffer);
    }

    @Override
    public long readInt64() {
        return VarIntUtils.readRawVarInt64(byteBuffer);
    }

    @Override
    public long readSInt64() {
        return VarIntUtils.readRawVarInt64(byteBuffer, true);
    }

    @Override
    public double readDouble() {
        return BytesUtils.readDoubleLE(byteBuffer);
    }

    @Override
    public String readString(int len) {
        if (len == 0) {
            return null;
        }
        else if (len == 1) {
            return "";
        }

        int position = byteBuffer.position();
        String result;
        if (byteBuffer.hasArray()) {
            //内置数组, 即heap bytebuffer
            if (UnsafeUtil.hasUnsafe()) {
                result = UnsafeUtf8Util.decodeUtf8(byteBuffer.array(), byteBuffer.arrayOffset() + position, len);
            } else {
                result = new String(byteBuffer.array(), byteBuffer.arrayOffset() + position, len, StandardCharsets.UTF_8);
            }
            byteBuffer.position(position + len);
        } else {
            //direct bytebuffer
            if (UnsafeUtil.hasUnsafe()) {
                result = UnsafeUtf8Util.decodeUtf8Direct(byteBuffer, position, len);
                byteBuffer.position(position + len);
            } else {
                byte[] tmp = new byte[len];
                byteBuffer.get(tmp);
                result = new String(tmp, StandardCharsets.UTF_8);
            }
        }
        return result;
    }

    @Override
    public final void fixReadIndex() {
        if (Objects.isNull(inputBuf)) {
            throw new UnsupportedOperationException("inputBuf is null, so this method is not supported");
        }
        ByteBufUtils.fixByteBufReadIndex(inputBuf, byteBuffer);
    }
}
