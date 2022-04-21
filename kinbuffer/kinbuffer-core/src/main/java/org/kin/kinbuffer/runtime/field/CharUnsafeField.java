package org.kin.kinbuffer.runtime.field;

import org.kin.framework.utils.UnsafeUtil;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.lang.reflect.Field;

/**
 * 通过unsafe读写char
 * @author huangjianqin
 * @date 2022/4/21
 */
public final class CharUnsafeField extends PrimitiveUnsafeField{
    public CharUnsafeField(int number, Field field) {
        super(number, field);
    }

    @Override
    public void merge(Input input, Object message) {
        UnsafeUtil.putChar(message, address, (char) input.readInt32());
    }

    @Override
    public void write(Output output, Object message, boolean end) {
        super.write(output, message, end);
        output.writeInt32(UnsafeUtil.getChar(message, address));
    }
}
