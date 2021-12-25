package org.kin.kinbuffer.io;

import io.netty.buffer.ByteBuf;
import org.kin.framework.io.ByteBufferOutput;
import org.kin.framework.io.StreamOutput;
import org.kin.framework.utils.BytesUtils;
import org.kin.framework.utils.VarIntUtils;
import org.kin.transport.netty.utils.ByteBufOutput;

import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
public class DefaultOutput implements Output {
    private final org.kin.framework.io.Output output;

    public static DefaultOutput stream(OutputStream outputStream) {
        return output(new StreamOutput(outputStream));
    }

    public static DefaultOutput buffer(ByteBuffer byteBuffer) {
        return output(new ByteBufferOutput(byteBuffer));
    }

    public static DefaultOutput buffer(ByteBuf byteBuf) {
        return output(new ByteBufOutput(byteBuf));
    }

    public static DefaultOutput output(org.kin.framework.io.Output output) {
        return new DefaultOutput(output);
    }

    public DefaultOutput(org.kin.framework.io.Output output) {
        this.output = output;
    }

    @Override
    public Output writeBoolean(boolean value) {
        output.writeByte(value ? 1 : 0);
        return this;
    }

    @Override
    public Output writeBytes(byte[] value) {
        output.writeBytes(value);
        return this;
    }

    @Override
    public Output writeByte(int value) {
        output.writeByte(value);
        return this;
    }

    @Override
    public Output writeInt32(int value) {
        VarIntUtils.writeRawVarInt32(output, value, false);
        return this;
    }

    @Override
    public Output writeSInt32(int value) {
        VarIntUtils.writeRawVarInt32(output, value);
        return this;
    }

    @Override
    public Output writeFloat(float value) {
        BytesUtils.writeFloatLE(output, value);
        return this;
    }

    @Override
    public Output writeInt64(long value) {
        VarIntUtils.writeRawVarInt64(output, value, false);
        return this;
    }

    @Override
    public Output writeSInt64(int value) {
        VarIntUtils.writeRawVarInt64(output, value);
        return this;
    }

    @Override
    public Output writeDouble(double value) {
        BytesUtils.writeDoubleLE(output, value);
        return this;
    }
}
