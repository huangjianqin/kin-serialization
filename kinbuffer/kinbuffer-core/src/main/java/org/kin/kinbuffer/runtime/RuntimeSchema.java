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
        for (org.kin.kinbuffer.runtime.field.Field field : fields) {
            //read from input and set field value
            field.merge(input, t);
        }
    }

    @Override
    public void write(Output output, T t) {
        for (org.kin.kinbuffer.runtime.field.Field field : fields) {
            //write field value to output
            field.write(output, t);
        }
    }
}
