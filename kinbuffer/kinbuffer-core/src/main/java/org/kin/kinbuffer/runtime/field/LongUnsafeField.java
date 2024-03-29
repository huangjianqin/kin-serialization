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
    public LongUnsafeField(Field field) {
        super(field);
    }

    @Override
    public void merge(Input input, Object message) {
        UnsafeUtil.putLong(message, address, isSigned()?input.readSVarInt64():input.readVarInt64());
    }

    @Override
    public void write(Output output, Object message) {
        long l = UnsafeUtil.getLong(message, address);
        if (isSigned()) {
            output.writeSVarInt64(l);
        }
        else{
            output.writeVarInt64(l);
        }
    }
}