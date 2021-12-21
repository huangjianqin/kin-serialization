package org.kin.kinbuffer.schema;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * primitive, Pojo(user custom class), 集合类(collection, set, map, array) Schema
 * user一般只需要继承实现Schema接口即可, 不需考虑复杂的集合类(collection, set, map, array)处理
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
