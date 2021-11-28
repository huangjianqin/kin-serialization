package org.kin.serialization.kryo.io;

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.Input;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * @author huangjianqin
 * @date 2021/11/28
 */
public final class Inputs {
    public static Input getInput(ByteBuf byteBuf) {
        return getInput(byteBuf.nioBuffer());
    }

    public static Input getInput(ByteBuffer byteBuffer) {
        ByteBufferInput input = new ByteBufferInput();
        input.setBuffer(byteBuffer);
        return input;
    }

    public static Input getInput(byte[] bytes) {
        return new Input(bytes);
    }

    private Inputs() {
    }
}
