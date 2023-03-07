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
    public CharUnsafeField(Field field) {
        super(field);
    }

    @Override
    public void merge(Input input, Object message) {
        UnsafeUtil.putChar(message, address, (char) input.readVarInt32());
    }

    @Override
    public void write(Output output, Object message) {
        output.writeVarInt32(UnsafeUtil.getChar(message, address));
    }
}
