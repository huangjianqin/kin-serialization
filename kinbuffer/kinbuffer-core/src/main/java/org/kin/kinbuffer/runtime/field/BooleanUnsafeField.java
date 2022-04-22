package org.kin.kinbuffer.runtime.field;

import org.kin.framework.utils.UnsafeUtil;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.lang.reflect.Field;

/**
 * 通过unsafe读写boolean
 * @author huangjianqin
 * @date 2022/4/21
 */
public final class BooleanUnsafeField extends PrimitiveUnsafeField{
    public BooleanUnsafeField(Field field) {
        super(field);
    }

    @Override
    public void merge(Input input, Object message) {
        UnsafeUtil.putBoolean(message, address, input.readBoolean());
    }

    @Override
    public void write(Output output, Object message) {
        output.writeBoolean(UnsafeUtil.getBoolean(message, address));
    }
}
