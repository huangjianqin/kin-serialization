package org.kin.kinbuffer.schema;

import org.kin.kinbuffer.io.Input;

/**
 * 主要处理复杂的场景,不能直接new message
 * @author huangjianqin
 * @date 2021/12/20
 */
public interface PolymorphicSchema extends Schema<Object>{
    /**
     * 从{@code input}中读取指定message实例
     */
    Object read(Input input);

    @Override
    default Object newMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    default void merge(Input input, Object o) {
        throw new UnsupportedOperationException();
    }
}
