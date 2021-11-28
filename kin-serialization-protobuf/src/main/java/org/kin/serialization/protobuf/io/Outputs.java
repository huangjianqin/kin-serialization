package org.kin.serialization.protobuf.io;

import io.netty.buffer.ByteBuf;
import io.protostuff.Input;
import org.kin.framework.utils.UnsafeUtil;

import java.nio.ByteBuffer;

/**
 * 针对不同情况获取{@link Output}实例工具方法
 * Forked from <a href="https://github.com/fengjiachun/Jupiter">Jupiter</a>.
 * @author huangjianqin
 * @date 2021/11/28
 */
public final class Outputs {
    public static Output getOutput(ByteBuf outputBuf) {
        if (outputBuf.hasMemoryAddress() && UnsafeUtil.hasUnsafe()) {
            return new UnsafeNioBufOutput(outputBuf, -1, Integer.MAX_VALUE);
        } else {
            return new NioBufOutput(outputBuf, -1, Integer.MAX_VALUE);
        }
    }

    public static Output getOutput(ByteBuffer outputBuffer) {
        if (outputBuffer.isDirect() && UnsafeUtil.hasUnsafe()) {
            return new UnsafeNioBufOutput(outputBuffer, Integer.MAX_VALUE);
        } else {
            return new NioBufOutput(outputBuffer, Integer.MAX_VALUE);
        }
    }

    private Outputs() {
    }
}
