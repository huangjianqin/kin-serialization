package org.kin.kinbuffer.schema.field;

import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.UnsafeUtil;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.schema.Runtime;
import org.kin.kinbuffer.schema.RuntimeSchema;
import org.kin.kinbuffer.schema.Schema;

/**
 * @author huangjianqin
 * @date 2021/12/19
 */
@SuppressWarnings("rawtypes")
public class UnsafeField extends Field {
    private final long address;

    public UnsafeField(java.lang.reflect.Field field) {
        this(field, null);
    }

    public UnsafeField(java.lang.reflect.Field field, Schema schema) {
        super(field, schema);
        address = UnsafeUtil.objectFieldOffset(field);
    }

    @Override
    public void merge(Input input, Object message) {
        Object value = afterRead(Runtime.read(input, type, schema));
        if (Boolean.TYPE.equals(type)) {
            UnsafeUtil.putBoolean(message, address, (Boolean) value);
        } else if (Byte.TYPE.equals(type)) {
            UnsafeUtil.putByte(message, address, (Byte) value);
        } else if (Character.TYPE.equals(type)) {
            UnsafeUtil.putChar(message, address, (Character) value);
        } else if (Short.TYPE.equals(type)) {
            UnsafeUtil.putShort(message, address, (Short) value);
        } else if (Integer.TYPE.equals(type)) {
            UnsafeUtil.putInt(message, address, (Integer) value);
        } else if (Long.TYPE.equals(type)) {
            UnsafeUtil.putLong(message, address, (Long) value);
        } else if (Float.TYPE.equals(type)) {
            UnsafeUtil.putFloat(message, address, (Float) value);
        } else if (Double.TYPE.equals(type)) {
            UnsafeUtil.putDouble(message, address, (Double) value);
        } else {
            UnsafeUtil.putObject(message, address, value);
        }
    }

    @Override
    public void write(Output output, Object message) {
        Object value;
        if (Boolean.TYPE.equals(type)) {
            value = UnsafeUtil.getBoolean(message, address);
        } else if (Byte.TYPE.equals(type)) {
            value = UnsafeUtil.getByte(message, address);
        } else if (Character.TYPE.equals(type)) {
            value = UnsafeUtil.getChar(message, address);
        } else if (Short.TYPE.equals(type)) {
            value = UnsafeUtil.getShort(message, address);
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
        Runtime.write(output, beforeWrite(value), schema);
    }
}
