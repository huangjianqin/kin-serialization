package org.kin.kinbuffer.runtime;

import io.netty.util.collection.IntObjectHashMap;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.field.Field;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

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
    private Supplier<T> constructor;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public RuntimeSchema(Class typeClass, List<org.kin.kinbuffer.runtime.field.Field> fields) {
        this.typeClass = typeClass;
        this.fieldMap = new IntObjectHashMap<>();
        for (Field field : fields) {
            this.fieldMap.put(field.getNumber(), field);
        }
        try {
            //生成代理message构造方法的Supplier实例
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle constructorHandle = lookup.unreflectConstructor(typeClass.getConstructor());
            constructor = (Supplier<T>) LambdaMetafactory.metafactory(lookup, "get",
                    MethodType.methodType(Supplier.class),
                    MethodType.methodType(typeClass).erase(),
                    constructorHandle,
                    MethodType.methodType(typeClass)
            ).getTarget().invoke();
        } catch (Throwable e) {
            ExceptionUtils.throwExt(e);
        }
    }

    @Override
    public T newMessage() {
        return constructor.get();
    }

    @Override
    public void merge(Input input, T t) {
        while(true){
            int tag = input.readInt32();
            //{number: n bit}{field null or not: 1bit}{field is tail: 1bit}
            int number = tag >> 2;
            boolean nonNull = (tag & 2) == 2;
            boolean tail = (tag & 1) == 1;

            if(nonNull){
                //read from input and set field value
                fieldMap.get(number).merge(input, t);
            }

            if(tail){
                break;
            }
        }
    }

    @Override
    public void write(Output output, T t) {
        int i = 0;
        int size = fieldMap.size();
        Iterator<Field> iterator = fieldMap.values().iterator();
        while(iterator.hasNext()){
            Field field = iterator.next();
            //write field value to output
            field.writeWithFieldNumber(output, t, i == size -1);
            i++;
        }
    }
}
