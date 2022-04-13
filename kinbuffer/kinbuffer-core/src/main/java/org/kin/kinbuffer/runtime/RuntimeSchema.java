package org.kin.kinbuffer.runtime;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatchers;
import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.field.ByteBuddyField;
import org.kin.kinbuffer.runtime.field.Field;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author huangjianqin
 * @date 2021/12/11
 */
final class RuntimeSchema<T> implements Schema<T> {
    /** pojo类型 */
    private final Class<T> typeClass;
    /** 该pojo fields */
    private final List<org.kin.kinbuffer.runtime.field.Field> fields;
    /** 该pojo constructor */
    private Supplier<T> constructor;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public RuntimeSchema(Class typeClass, List<org.kin.kinbuffer.runtime.field.Field> fields) {
        this.typeClass = typeClass;
        this.fields = fields;
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
                fields.get(number).merge(input, t);
            }

            if(tail){
                break;
            }
        }
    }

    @Override
    public void write(Output output, T t) {
        int size = fields.size();
        for (int i = 0; i < size; i++) {
            Field field = fields.get(i);
            //write field value to output
            field.write(output, t, i == size -1);
        }
    }
}
