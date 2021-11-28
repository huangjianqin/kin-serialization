package org.kin.serialization.protobuf.io;

import io.netty.buffer.ByteBuf;
import io.protostuff.ByteArrayInput;
import io.protostuff.Input;
import io.protostuff.ProtobufException;
import org.kin.framework.utils.UnsafeUtil;

import java.nio.ByteBuffer;

/**
 * Forked from <a href="https://github.com/fengjiachun/Jupiter">Jupiter</a>.
 * @author huangjianqin
 * @date 2021/11/27
 */
public final class Inputs {
    public static Input getInput(ByteBuf byteBuf) {
        if (byteBuf.hasMemoryAddress() && UnsafeUtil.hasUnsafe()) {
            return new UnsafeNioBufInput(byteBuf.nioBuffer(), true);
        } else {
            return new NioBufInput(byteBuf.nioBuffer(), true);
        }
    }

    public static Input getInput(ByteBuffer byteBuffer) {
        if (UnsafeUtil.hasUnsafe()) {
            return new UnsafeNioBufInput(byteBuffer, true);
        } else {
            return new NioBufInput(byteBuffer, true);
        }
    }

    public static Input getInput(byte[] bytes) {
        return getInput(bytes, 0, bytes.length);
    }

    public static Input getInput(byte[] bytes, int offset, int length) {
        return new ByteArrayInput(bytes, offset, length, true);
    }

    /**
     * 检查protobuf 协议bytes最后tag是否是0, 即结束
     */
    public static void checkEnd(Input input) throws ProtobufException{
        checkLastTagWas(input, 0);
    }

    /**
     * 检查protobuf 协议bytes最后tag是否指定值{@code value}
     */
    public static void checkLastTagWas(Input input, int value) throws ProtobufException {
        if (input instanceof UnsafeNioBufInput) {
            ((UnsafeNioBufInput) input).checkLastTagWas(value);
        } else if (input instanceof NioBufInput) {
            ((NioBufInput) input).checkLastTagWas(value);
        } else if (input instanceof ByteArrayInput) {
            ((ByteArrayInput) input).checkLastTagWas(value);
        }
    }

    private Inputs() {}
}
