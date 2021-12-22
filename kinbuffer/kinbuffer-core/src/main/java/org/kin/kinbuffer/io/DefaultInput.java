package org.kin.kinbuffer.io;

import org.kin.framework.utils.BytesUtils;
import org.kin.framework.utils.VarIntUtils;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
public class DefaultInput implements Input {
    private final org.kin.framework.io.Input input;

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
    public int readInt() {
        return VarIntUtils.readRawVarInt32(input, false);
    }

    @Override
    public float readFloat() {
        return BytesUtils.readFloatLE(input);
    }

    @Override
    public long readLong() {
        return VarIntUtils.readRawVarInt64(input, false);
    }

    @Override
    public double readDouble() {
        return BytesUtils.readDoubleLE(input);
    }
}
