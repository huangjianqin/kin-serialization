package org.kin.kinbuffer.schema.field;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.schema.Schema;

import java.lang.reflect.Type;

/**
 * @author huangjianqin
 * @date 2021/12/19
 */
public abstract class Field {
    /** 字段类型 */
    protected final Class type;
    protected final Schema schema;

    protected Field(Class type, Schema schema) {
        this.type = type;
        this.schema = schema;
    }

    /**
     * 读取bytes, 并给对应字段赋值
     *
     * @param message   消息实例, 读取字段值并赋值给消息
     */
    public abstract void merge(Input input, Object message);

    /**
     * 将{@code T}实例所有字段转换成bytes
     *
     * @param message   消息实例, 从消息读取字段值并写出
     */
    public abstract void write(Output output, Object message);
}
