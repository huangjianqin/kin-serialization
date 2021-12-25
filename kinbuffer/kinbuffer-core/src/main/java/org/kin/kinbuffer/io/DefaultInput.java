package org.kin.kinbuffer.io;

import io.netty.buffer.ByteBuf;
import org.kin.framework.io.ByteBufferInput;
import org.kin.framework.io.StreamInput;
import org.kin.framework.utils.BytesUtils;
import org.kin.framework.utils.VarIntUtils;
import org.kin.transport.netty.utils.ByteBufInput;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
public class DefaultInput implements Input {
    private final org.kin.framework.io.Input input;

    public static DefaultInput stream(InputStream inputStream) {
        return input(new StreamInput(inputStream));
    }

    public static DefaultInput buffer(ByteBuffer byteBuffer) {
        return input(new ByteBufferInput(byteBuffer));
    }

    public static DefaultInput buffer(ByteBuf byteBuf) {
        return input(new ByteBufInput(byteBuf));
    }

    public static DefaultInput input(org.kin.framework.io.Input input) {
        return new DefaultInput(input);
    }

    public DefaultInput(org.kin.framework.io.Input input) {
        this.input = input;
    }

    @Override
    public boolean readBoolean() {
        return input.readByte() != 0;
    }

    @Override
    public byte[] readBytes(int length) {
        byte[] ret = new byte[length];
        input.readBytes(ret);
        return ret;
    }

    @Override
    public byte[] readBytes() {
        return readBytes(input.readableBytes());
    }

    @Override
    public int readByte() {
        return input.readByte();
    }

    @Override
    public int readInt32() {
        return VarIntUtils.readRawVarInt32(input, false);
    }

    @Override
    public int readSInt32() {
        return VarIntUtils.readRawVarInt32(input);
    }

    @Override
    public float readFloat() {
        return BytesUtils.readFloatLE(input);
    }

    @Override
    public long readInt64() {
        return VarIntUtils.readRawVarInt64(input, false);
    }

    @Override
    public long readSInt64() {
        return VarIntUtils.readRawVarInt64(input);
    }

    @Override
    public double readDouble() {
        return BytesUtils.readDoubleLE(input);
    }
}
