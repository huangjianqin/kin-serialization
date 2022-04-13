package org.kin.kinbuffer.runtime.field;

import org.kin.framework.utils.ExceptionUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.Runtime;
import org.kin.kinbuffer.runtime.Schema;

/**
 * 基于反射的field处理
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
        super(field, schema);
        this.field = field;
        field.setAccessible(true);
    }

    @Override
    protected void set(Object message, Object rawValue) {
        try {
            field.set(message, afterRead(rawValue));
        } catch (IllegalAccessException e) {
            ExceptionUtils.throwExt(e);
        }
    }

    @Override
    protected Object get(Object message) {
        try {
            return beforeWrite(field.get(message));
        } catch (IllegalAccessException e) {
            ExceptionUtils.throwExt(e);
        }

        return null;
    }
}
