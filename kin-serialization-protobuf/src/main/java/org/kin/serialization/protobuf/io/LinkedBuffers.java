package org.kin.serialization.protobuf.io;

import io.protostuff.LinkedBuffer;

import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2021/11/27
 */
public final class LinkedBuffers {
    /** 避免每次序列化都重新申请Buffer空间 */
    private static final ThreadLocal<LinkedBuffer> BUFFER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 获取本线程绑定的{@link LinkedBuffer}实例
     */
    public static LinkedBuffer getLinkedBuffer() {
        LinkedBuffer linkedBuffer = BUFFER_THREAD_LOCAL.get();
        if (Objects.isNull(linkedBuffer)) {
            linkedBuffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
            BUFFER_THREAD_LOCAL.set(linkedBuffer);
        }
        return linkedBuffer;
    }

    /**
     * clean {@link LinkedBuffer}占用空间
     */
    public static void clearBuffer() {
        getLinkedBuffer().clear();
    }

    /**
     * clean {@link LinkedBuffer}占用空间
     */
    public static void clearBuffer(LinkedBuffer buf) {
        buf.clear();
    }

    private LinkedBuffers() {}
}
