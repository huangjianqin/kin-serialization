package org.kin.kinbuffer.io;

import io.netty.buffer.ByteBuf;
import org.kin.framework.concurrent.FastThreadLocal;
import org.kin.framework.io.ScalableByteArray;
import org.kin.framework.utils.UnsafeUtil;

import java.nio.ByteBuffer;

/**
 * @author huangjianqin
 * @date 2022/4/16
 */
public final class Outputs {
    private static final FastThreadLocal<ByteArrayOutput> THREAD_LOCAL_ARRAY = new FastThreadLocal<ByteArrayOutput>() {
        @Override
        protected ByteArrayOutput initialValue() {
            return new ByteArrayOutput(new ScalableByteArray(256));
        }
    };

    private Outputs() {
    }

    /**
     * 获取底层基于{@link ByteBuf}的{@link Output}实现
     */
    public static NioBufOutput getOutput(ByteBuf byteBuf) {
        if (byteBuf.hasMemoryAddress() && UnsafeUtil.hasUnsafe()) {
            //direct bytebuffer
            return new UnsafeNioBufOutput(byteBuf);
        } else {
            return new NioBufOutput(byteBuf);
        }
    }

    /**
     * 获取底层基于{@link ByteBuffer}的{@link Output}实现
     */
    public static NioBufOutput getOutput(ByteBuffer byteBuffer) {
        if (byteBuffer.isDirect() && UnsafeUtil.hasUnsafe()) {
            //direct bytebuffer
            return new UnsafeNioBufOutput(byteBuffer);
        } else {
            return new NioBufOutput(byteBuffer);
        }
    }

    /**
     * 获取底层基于bytes的{@link Output}实现
     */
    public static ByteArrayOutput getOutput() {
        return THREAD_LOCAL_ARRAY.get();
    }

    /**
     * 重置线程缓存的{@link ByteArrayOutput}
     */
    public static void clearByteArrayOutput(){
        THREAD_LOCAL_ARRAY.get().clear();
    }
}
