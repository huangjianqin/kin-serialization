package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
@SuppressWarnings("rawtypes")
final class MessageArraySchema extends PolymorphicSchema {
    /** 多维class定义 */
    private final List<Class<?>> classes;
    /** 维度 */
    private final int lv;

    MessageArraySchema(Class<?> type) {
        if (!type.isArray()) {
            throw new IllegalArgumentException(String.format("type '%s' is not a array", type.getName()));
        }

        //多维数组
        int lv = 0;
        List<Class<?>> classes = new ArrayList<>();
        while(type.isArray()){
            lv++;
            type = type.getComponentType();
            classes.add(type);
        }
        Collections.reverse(classes);
        this.classes = Collections.unmodifiableList(classes);
        this.lv = lv;
    }

    /**
     * 获取下一维度的数组类型
     */
    private Class<?> getNextLvClass(int lv){
        return classes.get(lv - 1);
    }

    /**
     * 根据item type获取{@link Schema}实现
     */
    private Schema<?> getSchema(Class<?> itemType){
        if (!itemType.isPrimitive() &&
                (Object.class.equals(itemType) || Modifier.isAbstract(itemType.getModifiers()))) {
            return ObjectSchema.INSTANCE;
        }
        else{
            return Runtime.getSchema(itemType);
        }
    }

    @Override
    public Object read(Input input) {
        return read(input, lv, getNextLvClass(lv));
    }

    private Object read(Input input, int lv, Class<?> itemType){
        if(lv > 1){
            int len = input.readInt32();
            Object[] arr = (Object[]) Array.newInstance(itemType, len);
            lv--;
            for (int i = 0; i < len; i++) {
                arr[i] = read(input, lv, getNextLvClass(lv));
            }
            return arr;
        }

        Schema<?> schema = getSchema(itemType);

        int len = input.readInt32();

        //这样子处理是因为primitive[]无法cast to Object[]
        if (Boolean.TYPE.equals(itemType)) {
            boolean[] arr = (boolean[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (boolean) SchemaUtils.read(input, schema);
            }
            return arr;
        } else if (Byte.TYPE.equals(itemType)) {
            byte[] arr = (byte[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (byte) SchemaUtils.read(input, schema);
            }
            return arr;
        } else if (Character.TYPE.equals(itemType)) {
            char[] arr = (char[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (char) SchemaUtils.read(input, schema);
            }
            return arr;
        } else if (Short.TYPE.equals(itemType)) {
            short[] arr = (short[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (short) SchemaUtils.read(input, schema);
            }
            return arr;
        } else if (Integer.TYPE.equals(itemType)) {
            int[] arr = (int[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (int) SchemaUtils.read(input, schema);
            }
            return arr;
        } else if (Long.TYPE.equals(itemType)) {
            long[] arr = (long[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (long) SchemaUtils.read(input, schema);
            }
            return arr;
        } else if (Float.TYPE.equals(itemType)) {
            float[] arr = (float[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (float) SchemaUtils.read(input, schema);
            }
            return arr;
        } else if (Double.TYPE.equals(itemType)) {
            double[] arr = (double[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = (double) SchemaUtils.read(input, schema);
            }
            return arr;
        } else {
            Object[] arr = (Object[]) Array.newInstance(itemType, len);
            for (int i = 0; i < len; i++) {
                arr[i] = SchemaUtils.read(input, schema);
            }
            return arr;
        }
    }

    @Override
    public void write(Output output, Object t) {
        if (Objects.isNull(t)) {
            output.writeInt32(0);
            return;
        }

        write(output, t, lv, getNextLvClass(lv));
    }

    private void write(Output output, Object t, int lv, Class<?> itemType){
        if(lv > 1){
            Object[] arr = (Object[]) t;
            int len = arr.length;
            lv--;
            output.writeInt32(len);
            for (Object item : arr) {
                write(output, item, lv, getNextLvClass(lv));
            }
            return;
        }

        Schema<?> schema = getSchema(itemType);

        //这样子处理是因为primitive[]无法cast to Object[]
        if (Boolean.TYPE.equals(itemType)) {
            boolean[] arr = (boolean[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (boolean item : arr) {
                SchemaUtils.write(output, item, schema);
            }
        } else if (Byte.TYPE.equals(itemType)) {
            byte[] arr = (byte[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (byte item : arr) {
                SchemaUtils.write(output, item, schema);
            }
        } else if (Character.TYPE.equals(itemType)) {
            char[] arr = (char[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (char item : arr) {
                SchemaUtils.write(output, item, schema);
            }
        } else if (Short.TYPE.equals(itemType)) {
            short[] arr = (short[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (short item : arr) {
                SchemaUtils.write(output, item, schema);
            }
        } else if (Integer.TYPE.equals(itemType)) {
            int[] arr = (int[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (int item : arr) {
                SchemaUtils.write(output, item, schema);
            }
        } else if (Long.TYPE.equals(itemType)) {
            long[] arr = (long[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (long item : arr) {
                SchemaUtils.write(output, item, schema);
            }
        } else if (Float.TYPE.equals(itemType)) {
            float[] arr = (float[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (float item : arr) {
                SchemaUtils.write(output, item, schema);
            }
        } else if (Double.TYPE.equals(itemType)) {
            double[] arr = (double[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (double item : arr) {
                SchemaUtils.write(output, item, schema);
            }
        } else {
            Object[] arr = (Object[]) t;
            int len = arr.length;
            output.writeInt32(len);
            for (Object item : arr) {
                SchemaUtils.write(output, item, schema);
            }
        }
    }
}
