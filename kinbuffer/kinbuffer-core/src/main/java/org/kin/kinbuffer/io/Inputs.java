package org.kin.kinbuffer.io;

import io.netty.buffer.ByteBuf;
import org.kin.framework.utils.UnsafeUtil;

import java.nio.ByteBuffer;

/**
 * {@link Input}实现类工具方法
 *
 * @author huangjianqin
 * @date 2022/4/15
 */
public final class Inputs {
    private Inputs() {
    }

    /**
     * 获取从{@link ByteBuf}读取bytes的{@link Input}实现
     */
    public static Input getInput(ByteBuf byteBuf) {
        if (byteBuf.hasMemoryAddress() && UnsafeUtil.hasUnsafe()) {
            //direct bytebuffer
            return new UnsafeNioBufInput(byteBuf);
        } else {
            return new NioBufInput(byteBuf);
        }
    }

    /**
     * 获取从{@link ByteBuffer}读取bytes的{@link Input}实现
     */
    public static Input getInput(ByteBuffer byteBuffer) {
        if (byteBuffer.isDirect() && UnsafeUtil.hasUnsafe()) {
            //direct bytebuffer
            return new UnsafeNioBufInput(byteBuffer);
        } else {
            return new NioBufInput(byteBuffer);
        }
    }

    /**
     * 获取从{@code byte[]}读取bytes的{@link Input}实现
     */
    public static Input getInput(byte[] bytes) {
        return new ByteArrayInput(bytes);
    }
}
