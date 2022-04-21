package org.kin.kinbuffer.runtime.field;

import org.kin.framework.utils.UnsafeUtil;
import org.kin.kinbuffer.runtime.Schema;

/**
 * 基于{@link sun.misc.Unsafe}的field处理
 *
 * @author huangjianqin
 * @date 2021/12/19
 */
@SuppressWarnings("rawtypes")
public final class UnsafeField extends ObjectField {
    /** 内存地址 */
    private final long address;

    public UnsafeField(int number, java.lang.reflect.Field field) {
        this(number, field, null);
    }

    public UnsafeField(int number, java.lang.reflect.Field field, Schema schema) {
        super(number, field, schema);
        //获取内存地址
        address = UnsafeUtil.objectFieldOffset(field);
    }

    @Override
    protected void set(Object message, Object rawValue) {
        UnsafeUtil.putObject(message, address, afterRead(rawValue));
    }

    @Override
    protected Object get(Object message) {
        Object value = UnsafeUtil.getObject(message, address);
        return beforeWrite(value);
    }
}
