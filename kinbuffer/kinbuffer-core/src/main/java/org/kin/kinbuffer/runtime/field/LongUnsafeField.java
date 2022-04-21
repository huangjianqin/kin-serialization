package org.kin.kinbuffer.runtime.field;

import org.kin.framework.utils.UnsafeUtil;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.lang.reflect.Field;

/**
 * 通过unsafe读写long
 * @author huangjianqin
 * @date 2022/4/21
 */
public final class LongUnsafeField extends PrimitiveUnsafeField{
    public LongUnsafeField(int number, Field field) {
        super(number, field);
    }

    @Override
    public void merge(Input input, Object message) {
        UnsafeUtil.putLong(message, address, isSigned()?input.readSInt64():input.readInt64());
    }

    @Override
    public void write(Output output, Object message, boolean end) {
        super.write(output, message, end);
        long l = UnsafeUtil.getLong(message, address);
        if (isSigned()) {
            output.writeSInt64(l);
        }
        else{
            output.writeInt64(l);
        }
    }
}