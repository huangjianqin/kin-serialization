package org.kin.kinbuffer.runtime.field;

import org.kin.framework.utils.UnsafeUtil;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.lang.reflect.Field;

/**
 * 通过unsafe读写float
 * @author huangjianqin
 * @date 2022/4/21
 */
public final class FloatUnsafeField extends PrimitiveUnsafeField{
    public FloatUnsafeField(int number, Field field) {
        super(number, field);
    }

    @Override
    public void merge(Input input, Object message) {
        UnsafeUtil.putFloat(message, address, input.readFloat());
    }

    @Override
    public void write(Output output, Object message, boolean end) {
        super.write(output, message, end);
        output.writeFloat(UnsafeUtil.getFloat(message, address));
    }
}