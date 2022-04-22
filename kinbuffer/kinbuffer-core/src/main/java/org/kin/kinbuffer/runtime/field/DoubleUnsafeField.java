package org.kin.kinbuffer.runtime.field;

import org.kin.framework.utils.UnsafeUtil;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.lang.reflect.Field;

/**
 * 通过unsafe读写double
 * @author huangjianqin
 * @date 2022/4/21
 */
public final class DoubleUnsafeField extends PrimitiveUnsafeField{
    public DoubleUnsafeField(Field field) {
        super(field);
    }

    @Override
    public void merge(Input input, Object message) {
        UnsafeUtil.putDouble(message, address, input.readDouble());
    }

    @Override
    public void write(Output output, Object message) {
        output.writeDouble(UnsafeUtil.getDouble(message, address));
    }
}