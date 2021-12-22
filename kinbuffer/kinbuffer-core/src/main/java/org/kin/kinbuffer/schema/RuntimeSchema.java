package org.kin.kinbuffer.schema;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatchers;
import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.schema.field.ByteBuddyField;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author huangjianqin
 * @date 2021/12/11
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class RuntimeSchema<T> implements Schema<T>{
    private final Class<T> typeClass;
    private final List<org.kin.kinbuffer.schema.field.Field> fields;
    private Supplier<T> constructor;

    public RuntimeSchema(Class typeClass, List<org.kin.kinbuffer.schema.field.Field> fields) {
        this.typeClass = typeClass;
        this.fields = fields;
        try {
            Class<? extends Supplier> supplierClass = new ByteBuddy()
                    .subclass(Supplier.class)
                    .name("Supplier" + typeClass.hashCode())
                    .method(ElementMatchers.named("get"))
                    .intercept(MethodCall.construct(typeClass.getConstructor()).withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                    .make()
                    .load(ByteBuddyField.class.getClassLoader())
                    .getLoaded();
            constructor = ClassUtils.instance(supplierClass);
        } catch (NoSuchMethodException e) {
            ExceptionUtils.throwExt(e);
        }
    }

    @Override
    public T newMessage() {
        return constructor.get();
    }

    @Override
    public void merge(Input input, T t) {
        for (org.kin.kinbuffer.schema.field.Field field : fields) {
            field.merge(input, t);
        }
    }

    @Override
    public void write(Output output, T t) {
        for (org.kin.kinbuffer.schema.field.Field field : fields) {
            field.write(output, t);
        }
    }
}
