package org.kin.kinbuffer.runtime.field;

import org.kin.framework.utils.UnsafeUtil;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.lang.reflect.Field;

/**
 * 通过unsafe读写byte
 * @author huangjianqin
 * @date 2022/4/21
 */
public final class ByteUnsafeField extends PrimitiveUnsafeField{
    public ByteUnsafeField(int number, Field field) {
        super(number, field);
    }

    @Override
    public void merge(Input input, Object message) {
        UnsafeUtil.putByte(message, address, (byte) (isSigned() ? input.readSInt32() : input.readInt32()));
    }

    @Override
    public void write(Output output, Object message, boolean end) {
        super.write(output, message, end);
        byte b = UnsafeUtil.getByte(message, address);
        if(isSigned()){
            output.writeSInt32(b);
        }
        else{
            output.writeInt32(b);
        }
    }
}
