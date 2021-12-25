package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
@SuppressWarnings("rawtypes")
final class MessageArraySchema<T> extends PolymorphicSchema {
    /** item类型 */
    private final Class<T> itemType;
    /** item schema, 如果为null, 则是pojo, lazy init */
    @Nullable
    private Schema schema;

    MessageArraySchema(Class<T> itemType) {
        this(itemType, null);
    }

    MessageArraySchema(Class<T> itemType, Schema schema) {
        this.itemType = itemType;
        this.schema = schema;
    }

    /**
     * lazy init schema
     */
    private void tryLazyInitSchema(){
        if (Objects.isNull(schema)) {
            schema = Runtime.getSchema(itemType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object read(Input input) {
        tryLazyInitSchema();
        int len = input.readInt32();

        //这样子处理是因为primitive[]无法cast to Object[]
        if (Boolean.TYPE.equals(itemType)) {
            boolean[] arr = (boolean[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (boolean) Runtime.read(input, schema);
            }
            return arr;
        } else if (Byte.TYPE.equals(itemType)) {
            byte[] arr = (byte[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (byte) Runtime.read(input, schema);
            }
            return arr;
        } else if (Character.TYPE.equals(itemType)) {
            char[] arr = (char[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (char) Runtime.read(input, schema);
            }
            return arr;
        } else if (Short.TYPE.equals(itemType)) {
            short[] arr = (short[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (short) Runtime.read(input, schema);
            }
            return arr;
        } else if (Integer.TYPE.equals(itemType)) {
            int[] arr = (int[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (int) Runtime.read(input, schema);
            }
            return arr;
        } else if (Long.TYPE.equals(itemType)) {
            long[] arr = (long[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (long) Runtime.read(input, schema);
            }
            return arr;
        } else if (Float.TYPE.equals(itemType)) {
            float[] arr = (float[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (float) Runtime.read(input, schema);
            }
            return arr;
        } else if (Double.TYPE.equals(itemType)) {
            double[] arr = (double[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (double) Runtime.read(input, schema);
            }
            return arr;
        } else {
            T[] arr = (T[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (T) Runtime.read(input, schema);
            }
            return arr;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(Output output, Object t) {
        tryLazyInitSchema();
        if (Objects.isNull(t)) {
            output.writeInt32(0);
            return;
        }

        //这样子处理是因为primitive[]无法cast to Object[]
        if (Boolean.TYPE.equals(itemType)) {
            boolean[] arr = (boolean[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (boolean item : arr) {
                Runtime.write(output, item, schema);
            }
        } else if (Byte.TYPE.equals(itemType)) {
            byte[] arr = (byte[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (byte item : arr) {
                Runtime.write(output, item, schema);
            }
        } else if (Character.TYPE.equals(itemType)) {
            char[] arr = (char[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (char item : arr) {
                Runtime.write(output, item, schema);
            }
        } else if (Short.TYPE.equals(itemType)) {
            short[] arr = (short[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (short item : arr) {
                Runtime.write(output, item, schema);
            }
        } else if (Integer.TYPE.equals(itemType)) {
            int[] arr = (int[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (int item : arr) {
                Runtime.write(output, item, schema);
            }
        } else if (Long.TYPE.equals(itemType)) {
            long[] arr = (long[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (long item : arr) {
                Runtime.write(output, item, schema);
            }
        } else if (Float.TYPE.equals(itemType)) {
            float[] arr = (float[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (float item : arr) {
                Runtime.write(output, item, schema);
            }
        } else if (Double.TYPE.equals(itemType)) {
            double[] arr = (double[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (double item : arr) {
                Runtime.write(output, item, schema);
            }
        } else {
            T[] arr = (T[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (T item : arr) {
                Runtime.write(output, item, schema);
            }
        }
    }
}
