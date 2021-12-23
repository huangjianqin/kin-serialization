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
public class MessageArraySchema<T> extends PolymorphicSchema {
    private final Class<T> typeClass;
    @Nullable
    private Schema schema;

    public MessageArraySchema(Class<T> typeClass) {
        this(typeClass, null);
    }

    public MessageArraySchema(Class<T> typeClass, Schema schema) {
        this.typeClass = typeClass;
        this.schema = schema;
    }

    /**
     * lazy init schema
     */
    private void tryLazyInitSchema(){
        if (Objects.isNull(schema)) {
            schema = Runtime.getSchema(typeClass);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object read(Input input) {
        tryLazyInitSchema();
        int len = input.readInt();

        if (Boolean.TYPE.equals(typeClass)) {
            boolean[] arr = (boolean[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (boolean) Runtime.read(input, schema);
            }
            return arr;
        } else if (Byte.TYPE.equals(typeClass)) {
            byte[] arr = (byte[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (byte) Runtime.read(input, schema);
            }
            return arr;
        } else if (Character.TYPE.equals(typeClass)) {
            char[] arr = (char[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (char) Runtime.read(input, schema);
            }
            return arr;
        } else if (Short.TYPE.equals(typeClass)) {
            short[] arr = (short[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (short) Runtime.read(input, schema);
            }
            return arr;
        } else if (Integer.TYPE.equals(typeClass)) {
            int[] arr = (int[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (int) Runtime.read(input, schema);
            }
            return arr;
        } else if (Long.TYPE.equals(typeClass)) {
            long[] arr = (long[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (long) Runtime.read(input, schema);
            }
            return arr;
        } else if (Float.TYPE.equals(typeClass)) {
            float[] arr = (float[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (float) Runtime.read(input, schema);
            }
            return arr;
        } else if (Double.TYPE.equals(typeClass)) {
            double[] arr = (double[]) Array.newInstance(typeClass, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (double) Runtime.read(input, schema);
            }
            return arr;
        } else {
            T[] arr = (T[]) Array.newInstance(typeClass, len);
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
