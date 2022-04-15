package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;

/**
 * 主要处理不能直接new message的场景
 *
 * @author huangjianqin
 * @date 2021/12/20
 */
public abstract class PolymorphicSchema<T> implements Schema<T> {
    /**
     * 从{@code input}中读取bytes并构建message实例
     */
    public abstract T read(Input input);

    @Override
    public final T newMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void merge(Input input, T t) {
        throw new UnsupportedOperationException();
    }
}
