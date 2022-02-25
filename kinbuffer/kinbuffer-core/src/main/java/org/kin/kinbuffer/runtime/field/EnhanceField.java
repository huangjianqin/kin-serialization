package org.kin.kinbuffer.runtime.field;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.Runtime;
import org.kin.kinbuffer.runtime.Schema;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 使用字节码增强技术生成setter和getter代理方法的{@link Field}实现抽象
 * @author huangjianqin
 * @date 2022/2/25
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class EnhanceField extends Field {
    /** setter代理 */
    private final BiConsumer setter;
    /** getter代理 */
    private final Function getter;

    protected EnhanceField(java.lang.reflect.Field field, Schema schema) {
        super(field, schema);
        setter = genSetter(field);
        getter = genGetter(field);
    }

    protected abstract BiConsumer genSetter(java.lang.reflect.Field field);

    protected abstract Function genGetter(java.lang.reflect.Field field);

    @Override
    protected final void merge0(Input input, Object message) {
        Object value = afterRead(Runtime.read(input, schema));
        setter.accept(message, value);
    }

    @Override
    protected final void write0(Output output, Object message) {
        Object value = beforeWrite(getter.apply(message));
        Runtime.write(output, value, schema);
    }
}
