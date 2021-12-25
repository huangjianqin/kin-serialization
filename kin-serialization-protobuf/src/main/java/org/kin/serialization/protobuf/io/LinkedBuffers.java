package org.kin.serialization.protobuf.io;

import io.protostuff.LinkedBuffer;
import org.kin.framework.concurrent.FastThreadLocal;

/**
 * @author huangjianqin
 * @date 2021/11/27
 */
public final class LinkedBuffers {
    /** 避免每次序列化都重新申请Buffer空间 */
    private static final FastThreadLocal<LinkedBuffer> BUFFER_THREAD_LOCAL = new FastThreadLocal<LinkedBuffer>() {
        @Override
        protected LinkedBuffer initialValue(){
            return LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        }
    };

    /**
     * 获取本线程绑定的{@link LinkedBuffer}实例
     */
    public static LinkedBuffer getLinkedBuffer() {
        return BUFFER_THREAD_LOCAL.get();
    }

    /**
     * clean {@link LinkedBuffer}占用空间
     */
    public static void clearBuffer() {
        clearBuffer(getLinkedBuffer());
    }

    /**
     * clean {@link LinkedBuffer}占用空间
     */
    public static void clearBuffer(LinkedBuffer buf) {
        buf.clear();
    }

    private LinkedBuffers() {
    }
}
