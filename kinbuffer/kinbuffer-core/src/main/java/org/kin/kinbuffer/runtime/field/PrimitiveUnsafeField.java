package org.kin.kinbuffer.runtime.field;

import org.kin.framework.utils.UnsafeUtil;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.util.Objects;

/**
 * 字段类型是primitive
 * @author huangjianqin
 * @date 2022/4/21
 */
public abstract class PrimitiveUnsafeField extends Field{
    /** 内存地址 */
    protected final long address;

    protected PrimitiveUnsafeField(int number, java.lang.reflect.Field field) {
        super(number, field);
        //获取内存地址
        address = UnsafeUtil.objectFieldOffset(field);
    }

    @Override
    public void write(Output output, Object message, boolean end) {
        //primitive不会为空
        //{number: n bit}{field non null or not: 1bit}{field is tail: 1bit}
        int tag = (number << 1 | 1) << 1 | (end ? 1 : 0);
        output.writeInt32(tag);
    }
}
