package org.kin.kinbuffer.runtime;

import io.netty.util.collection.IntObjectHashMap;
import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.field.Field;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2021/12/11
 */
final class RuntimeSchema<T> implements Schema<T> {
    /** pojo类型 */
    private final Class<T> typeClass;
    /** key -> field number, value -> 对应field */
    private final IntObjectHashMap<org.kin.kinbuffer.runtime.field.Field> fieldMap;
    /** 该pojo constructor */
    private Constructor<T> constructor;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public RuntimeSchema(Class typeClass, List<org.kin.kinbuffer.runtime.field.Field> fields) {
        this.typeClass = typeClass;
        this.fieldMap = new IntObjectHashMap<>();
        for (Field field : fields) {
            this.fieldMap.put(field.getNumber(), field);
        }
        //适用于于反序列化的构造器, 不会初始化对象
        constructor = ClassUtils.getNotLoadConstructor(typeClass);
    }

    @Override
    public T newMessage() {
        try {
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            ExceptionUtils.throwExt(e);
        }

        throw new IllegalStateException("encounter unknown error");
    }

    @Override
    public void merge(Input input, T message) {
        while (true) {
            int tag = input.readInt32();
            //{number: n bit}{field null or not: 1bit}{field is tail: 1bit}
            int number = tag >> 2;
            boolean nonNull = (tag & 2) == 2;
            boolean tail = (tag & 1) == 1;

            if (nonNull) {
                //read from input and set field value
                fieldMap.get(number).merge(input, message);
            }

            if (tail) {
                break;
            }
        }
    }

    @Override
    public void write(Output output, T message) {
        int i = 0;
        int size = fieldMap.size();
        Iterator<Field> iterator = fieldMap.values().iterator();
        while (iterator.hasNext()) {
            Field field = iterator.next();
            //write field value to output
            field.write(output, message, i == size - 1);
            i++;
        }
    }
}
