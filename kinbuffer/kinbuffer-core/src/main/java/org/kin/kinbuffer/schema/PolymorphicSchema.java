package org.kin.kinbuffer.schema;

import org.kin.kinbuffer.io.Input;

/**
 * 主要处理复不能直接new message的场景
 *
 * @author huangjianqin
 * @date 2021/12/20
 */
public abstract class PolymorphicSchema implements Schema<Object> {
    /**
     * 从{@code input}中读取指定message实例
     */
    public abstract Object read(Input input);

    @Override
    public final Object newMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void merge(Input input, Object o) {
        throw new UnsupportedOperationException();
    }
}
