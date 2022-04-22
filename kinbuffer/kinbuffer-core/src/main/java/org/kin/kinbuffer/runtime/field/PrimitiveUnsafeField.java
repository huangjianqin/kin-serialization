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

    protected PrimitiveUnsafeField(java.lang.reflect.Field field) {
        super(field);
        //获取内存地址
        address = UnsafeUtil.objectFieldOffset(field);
    }
}
