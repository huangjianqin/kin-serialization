package org.kin.kinbuffer.runtime.field;

import org.kin.framework.utils.UnsafeUtil;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.lang.reflect.Field;

/**
 * 通过unsafe读写int
 * @author huangjianqin
 * @date 2022/4/21
 */
public final class IntUnsafeField extends PrimitiveUnsafeField{
    public IntUnsafeField(int number, Field field) {
        super(number, field);
    }

    @Override
    public void merge(Input input, Object message) {
        UnsafeUtil.putInt(message, address, isSigned()?input.readSInt32():input.readInt32());
    }

    @Override
    public void write(Output output, Object message, boolean end) {
        super.write(output, message, end);
        int i = UnsafeUtil.getInt(message, address);
        if (isSigned()) {
            output.writeSInt32(i);
        }
        else {
            output.writeInt32(i);
        }
    }
}