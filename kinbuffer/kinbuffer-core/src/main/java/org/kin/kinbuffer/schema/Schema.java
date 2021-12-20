package org.kin.kinbuffer.schema;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * Pojo(user custom class)或者集合类(collection, set, map)的Schema
 * @author huangjianqin
 * @date 2021/12/11
 */
public interface Schema<T> {
    /**
     * @return  创建{@code T}实例
     */
    T newMessage();

    /**
     * 读取bytes, 并给对应字段赋值
     * @param input bytes input
     */
    void merge(Input input, T t);

    /**
     * 将{@code T}实例所有字段转换成bytes
     * @param t 实例
     */
    void write(Output output, T t);
}
