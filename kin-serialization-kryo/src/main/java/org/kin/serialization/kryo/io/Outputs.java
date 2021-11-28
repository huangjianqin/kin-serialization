package org.kin.serialization.kryo.io;

import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Output;
import io.netty.buffer.ByteBuf;
import org.kin.framework.concurrent.FastThreadLocal;
import org.kin.serialization.Serialization;

import java.nio.ByteBuffer;

/**
 * 获取{@link Output}工具方法
 * Forked from <a href="https://github.com/fengjiachun/Jupiter">Jupiter</a>.
 * @author huangjianqin
 * @date 2021/11/28
 */
public final class Outputs {
    /**
     * 复用{@link Output}
     */
    private static final FastThreadLocal<Output> OUTPUT_BYTES_THREAD_LOCAL = new FastThreadLocal<Output>() {
        @Override
        protected Output initialValue() {
            return new Output(Serialization.DEFAULT_BUF_SIZE, -1);
        }
    };

    public static ByteBufOutput getByteBufferOutput(ByteBuf byteBuf) {
        return new ByteBufOutput(byteBuf, -1, Integer.MAX_VALUE);
    }

    public static ByteBufferOutput getByteBufferOutput(ByteBuffer byteBuffer) {
        return new ByteBufferOutput(byteBuffer, Integer.MAX_VALUE);
    }

    public static Output getOutput() {
        return OUTPUT_BYTES_THREAD_LOCAL.get();
    }

    /**
     * 复用{@link Output}
     */
    public static void resetOutput(Output output) {
        //复用Output
        output.reset();

        // 防止hold过大的内存块一直不释放
        byte[] bytes = output.getBuffer();
        if (bytes == null) {
            return;
        }
        if (bytes.length > Serialization.MAX_CACHED_BUF_SIZE) {
            output.setBuffer(new byte[Serialization.DEFAULT_BUF_SIZE], -1);
        }
    }

    private Outputs() {
    }
}
