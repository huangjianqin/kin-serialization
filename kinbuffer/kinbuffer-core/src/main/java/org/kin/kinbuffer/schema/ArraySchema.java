package org.kin.kinbuffer.schema;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.lang.reflect.Array;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
@SuppressWarnings("rawtypes")
public class ArraySchema<T> extends NestSchema<Object> {
    private final Class<T> typeClass;

    public ArraySchema(Class<T> typeClass) {
        this(null, typeClass);
    }

    public ArraySchema(Schema schema, Class<T> typeClass) {
        super(schema);
        this.typeClass = typeClass;
    }

    @SuppressWarnings("unchecked")
    public Object read(Input input) {
        int len = input.readInt();

        if (Boolean.TYPE.equals(typeClass)) {
            boolean[] arr = (boolean[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (boolean) Runtime.read(input, typeClass, schema);
            }
            return arr;
        } else if (Byte.TYPE.equals(typeClass)) {
            byte[] arr = (byte[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (byte) Runtime.read(input, typeClass, schema);
            }
            return arr;
        } else if (Character.TYPE.equals(typeClass)) {
            char[] arr = (char[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (char) Runtime.read(input, typeClass, schema);
            }
            return arr;
        } else if (Short.TYPE.equals(typeClass)) {
            short[] arr = (short[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (short) Runtime.read(input, typeClass, schema);
            }
            return arr;
        } else if (Integer.TYPE.equals(typeClass)) {
            int[] arr = (int[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (int) Runtime.read(input, typeClass, schema);
            }
            return arr;
        } else if (Long.TYPE.equals(typeClass)) {
            long[] arr = (long[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (long) Runtime.read(input, typeClass, schema);
            }
            return arr;
        } else if (Float.TYPE.equals(typeClass)) {
            float[] arr = (float[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (float) Runtime.read(input, typeClass, schema);
            }
            return arr;
        } else if (Double.TYPE.equals(typeClass)) {
            double[] arr = (double[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (double) Runtime.read(input, typeClass, schema);
            }
            return arr;
        } else {
            T[] arr = (T[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (T) Runtime.read(input, typeClass, schema);
            }
            return arr;
        }
    }

    @Override
    public Object newMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void merge(Input input, Object o) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(Output output, Object t) {
        if (Objects.isNull(t)) {
            output.writeInt(0);
            return;
        }

        if (Boolean.TYPE.equals(typeClass)) {
            boolean[] arr = (boolean[]) t;
            int len = arr.length;
            output.writeInt(len);
            for (boolean item : arr) {
                Runtime.write(output, item, schema);
            }
        } else if (Byte.TYPE.equals(typeClass)) {
            byte[] arr = (byte[]) t;
            int len = arr.length;
            output.writeInt(len);
            for (byte item : arr) {
                Runtime.write(output, item, schema);
            }
        } else if (Character.TYPE.equals(typeClass)) {
            char[] arr = (char[]) t;
            int len = arr.length;
            output.writeInt(len);
            for (char item : arr) {
                Runtime.write(output, item, schema);
            }
        } else if (Short.TYPE.equals(typeClass)) {
            short[] arr = (short[]) t;
            int len = arr.length;
            output.writeInt(len);
            for (short item : arr) {
                Runtime.write(output, item, schema);
            }
        } else if (Integer.TYPE.equals(typeClass)) {
            int[] arr = (int[]) t;
            int len = arr.length;
            output.writeInt(len);
            for (int item : arr) {
                Runtime.write(output, item, schema);
            }
        } else if (Long.TYPE.equals(typeClass)) {
            long[] arr = (long[]) t;
            int len = arr.length;
            output.writeInt(len);
            for (long item : arr) {
                Runtime.write(output, item, schema);
            }
        } else if (Float.TYPE.equals(typeClass)) {
            float[] arr = (float[]) t;
            int len = arr.length;
            output.writeInt(len);
            for (float item : arr) {
                Runtime.write(output, item, schema);
            }
        } else if (Double.TYPE.equals(typeClass)) {
            double[] arr = (double[]) t;
            int len = arr.length;
            output.writeInt(len);
            for (double item : arr) {
                Runtime.write(output, item, schema);
            }
        } else {
            T[] arr = (T[]) t;
            int len = arr.length;
            output.writeInt(len);
            for (T item : arr) {
                Runtime.write(output, item, schema);
            }
        }
    }
}
