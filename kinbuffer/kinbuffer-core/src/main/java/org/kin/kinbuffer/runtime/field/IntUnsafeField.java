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
    public IntUnsafeField(Field field) {
        super(field);
    }

    @Override
    public void merge(Input input, Object message) {
        UnsafeUtil.putInt(message, address, isSigned()?input.readSVarInt32():input.readVarInt32());
    }

    @Override
    public void write(Output output, Object message) {
        int i = UnsafeUtil.getInt(message, address);
        if (isSigned()) {
            output.writeSVarInt32(i);
        }
        else {
            output.writeVarInt32(i);
        }
    }
}