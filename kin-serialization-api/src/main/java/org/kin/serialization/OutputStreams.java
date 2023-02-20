package org.kin.serialization;

import org.kin.framework.concurrent.FastThreadLocal;
import org.kin.framework.fieldupdater.FieldUpdaters;
import org.kin.framework.fieldupdater.ReferenceFieldUpdater;

import java.io.ByteArrayOutputStream;

/**
 * @author huangjianqin
 * @date 2021/11/28
 */
public final class OutputStreams {
    /** 更新{@link ByteArrayOutputStream#}中的 byte[] */
    private static final ReferenceFieldUpdater<ByteArrayOutputStream, byte[]> OUTPUT_STREAM_BUF_UPDATER =
            FieldUpdaters.newReferenceFieldUpdater(ByteArrayOutputStream.class, "buf");

    /**
     * 复用{@link ByteArrayOutputStream}中的 byte[]
     */
    private static final FastThreadLocal<ByteArrayOutputStream> OUTPUT_STREAM_FAST_THREAD_LOCAL = new FastThreadLocal<ByteArrayOutputStream>() {

        @Override
        protected ByteArrayOutputStream initialValue() {
            return new ByteArrayOutputStream(Serialization.DEFAULT_BUFFER_SIZE);
        }
    };

    /**
     * 获取线程绑定的{@link ByteArrayOutputStream#}实例
     */
    public static ByteArrayOutputStream getByteArrayOutputStream() {
        return OUTPUT_STREAM_FAST_THREAD_LOCAL.get();
    }

    public static void resetBuf(ByteArrayOutputStream stream) {
        //复用
        stream.reset();

        //防止hold过大的内存块一直不释放
        if (OUTPUT_STREAM_BUF_UPDATER.get(stream).length > Serialization.MAX_CACHED_BUF_SIZE) {
            OUTPUT_STREAM_BUF_UPDATER.set(stream, new byte[Serialization.DEFAULT_BUFFER_SIZE]);
        }
    }

    private OutputStreams() {
    }
}
