package org.kin.kinbuffer.schema.field;

import org.kin.framework.utils.ExceptionUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.schema.Runtime;
import org.kin.kinbuffer.schema.Schema;

/**
 * @author huangjianqin
 * @date 2021/12/19
 */
@SuppressWarnings("rawtypes")
public class ReflectionField extends Field {
    private final java.lang.reflect.Field field;

    public ReflectionField(java.lang.reflect.Field field) {
        this(field, null);
    }

    public ReflectionField(java.lang.reflect.Field field, Schema schema) {
        super(field.getType(), schema);
        this.field = field;
        field.setAccessible(true);
    }

    @Override
    public void merge(Input input, Object message) {
        Object value = Runtime.read(input, type, schema);
        try {
            field.set(message, value);
        } catch (IllegalAccessException e) {
            ExceptionUtils.throwExt(e);
        }
    }

    @Override
    public void write(Output output, Object message) {
        try {
            Object value = field.get(message);
            Runtime.write(output, value, schema);
        } catch (IllegalAccessException e) {
            ExceptionUtils.throwExt(e);
        }
    }
}
