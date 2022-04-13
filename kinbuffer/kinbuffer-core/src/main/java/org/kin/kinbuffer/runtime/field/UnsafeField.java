package org.kin.kinbuffer.runtime.field;

import org.kin.framework.utils.UnsafeUtil;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.Runtime;
import org.kin.kinbuffer.runtime.Schema;

/**
 * 基于{@link sun.misc.Unsafe}的field处理
 * @author huangjianqin
 * @date 2021/12/19
 */
@SuppressWarnings("rawtypes")
public class UnsafeField extends Field {
    /** 内存地址 */
    private final long address;

    public UnsafeField(java.lang.reflect.Field field) {
        this(field, null);
    }

    public UnsafeField(java.lang.reflect.Field field, Schema schema) {
        super(field, schema);
        //获取内存地址
        address = UnsafeUtil.objectFieldOffset(field);
    }

    @Override
    protected void set(Object message, Object rawValue)  {
        Object value = afterRead(rawValue);
        //primitive处理
        if (Boolean.TYPE.equals(type)) {
            UnsafeUtil.putBoolean(message, address, (boolean) value);
        } else if (Byte.TYPE.equals(type)) {
            UnsafeUtil.putByte(message, address, (byte) value);
        } else if (Character.TYPE.equals(type)) {
            UnsafeUtil.putChar(message, address, (char) value);
        } else if (Short.TYPE.equals(type)) {
            UnsafeUtil.putShort(message, address, (short) value);
        } else if (Integer.TYPE.equals(type)) {
            UnsafeUtil.putInt(message, address, (int) value);
        } else if (Long.TYPE.equals(type)) {
            UnsafeUtil.putLong(message, address, (long) value);
        } else if (Float.TYPE.equals(type)) {
            UnsafeUtil.putFloat(message, address, (float) value);
        } else if (Double.TYPE.equals(type)) {
            UnsafeUtil.putDouble(message, address, (double) value);
        } else {
            UnsafeUtil.putObject(message, address, value);
        }
    }

    @Override
    protected Object get(Object message) {
        Object value;
        //primitive处理
        if (Boolean.TYPE.equals(type)) {
            value = UnsafeUtil.getBoolean(message, address);
        } else if (Byte.TYPE.equals(type)) {
            value = UnsafeUtil.getByte(message, address);
        } else if (Character.TYPE.equals(type)) {
            value = (char)UnsafeUtil.getChar(message, address);
        } else if (Short.TYPE.equals(type)) {
            value = (short)UnsafeUtil.getShort(message, address);
        } else if (Integer.TYPE.equals(type)) {
            value = UnsafeUtil.getInt(message, address);
        } else if (Long.TYPE.equals(type)) {
            value = UnsafeUtil.getLong(message, address);
        } else if (Float.TYPE.equals(type)) {
            value = UnsafeUtil.getFloat(message, address);
        } else if (Double.TYPE.equals(type)) {
            value = UnsafeUtil.getDouble(message, address);
        } else {
            value = UnsafeUtil.getObject(message, address);
        }
        return beforeWrite(value);
    }
}
