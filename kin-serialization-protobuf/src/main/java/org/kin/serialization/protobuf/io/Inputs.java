package org.kin.serialization.protobuf.io;

import io.netty.buffer.ByteBuf;
import io.protostuff.ByteArrayInput;
import io.protostuff.Input;
import io.protostuff.ProtobufException;
import org.kin.framework.utils.UnsafeUtil;

import java.nio.ByteBuffer;

/**
 * 针对不同情况获取{@link Input}实例工具方法
 * Forked from <a href="https://github.com/fengjiachun/Jupiter">Jupiter</a>.
 * @author huangjianqin
 * @date 2021/11/27
 */
public final class Inputs {
    public static Input getInput(ByteBuf byteBuf) {
        if (byteBuf.hasMemoryAddress() && UnsafeUtil.hasUnsafe()) {
            //堆外内存
            return new UnsafeNioBufInput(byteBuf.nioBuffer(), true);
        } else {
            return new NioBufInput(byteBuf.nioBuffer(), true);
        }
    }

    public static Input getInput(ByteBuffer byteBuffer) {
        if (byteBuffer.isDirect() && UnsafeUtil.hasUnsafe()) {
            //堆外内存
            return new UnsafeNioBufInput(byteBuffer, true);
        } else {
            return new NioBufInput(byteBuffer, true);
        }
    }

    public static Input getInput(byte[] bytes) {
        return new ByteArrayInput(bytes,true);
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
