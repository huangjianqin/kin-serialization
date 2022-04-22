package org.kin.kinbuffer.runtime.field;

import org.kin.framework.utils.UnsafeUtil;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.lang.reflect.Field;

/**
 * 通过unsafe读写short
 * @author huangjianqin
 * @date 2022/4/21
 */
public final class ShortUnsafeField extends PrimitiveUnsafeField{
    public ShortUnsafeField(Field field) {
        super(field);
    }

    @Override
    public void merge(Input input, Object message) {
        UnsafeUtil.putShort(message, address, (short) (isSigned()?input.readSInt32():input.readInt32()));
    }

    @Override
    public void write(Output output, Object message) {
        int s = UnsafeUtil.getShort(message, address);
        if (isSigned()) {
            output.writeSInt32(s);
        }
        else{
            output.writeInt32(s);
        }
    }
}