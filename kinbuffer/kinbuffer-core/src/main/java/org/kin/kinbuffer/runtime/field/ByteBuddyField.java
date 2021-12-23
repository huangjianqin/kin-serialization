package org.kin.kinbuffer.runtime.field;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatchers;
import org.kin.framework.utils.ClassUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.Runtime;
import org.kin.kinbuffer.runtime.Schema;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author huangjianqin
 * @date 2021/12/19
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ByteBuddyField extends Field {
    private final BiFunction setter;
    private final Function getter;

    public ByteBuddyField(java.lang.reflect.Field field) {
        this(field, null);
    }

    public ByteBuddyField(java.lang.reflect.Field field, Schema schema) {
        super(field, schema);
        Method setterMethod = ClassUtils.setterMethod(field);
        if (Objects.isNull(setterMethod)) {
            throw new IllegalArgumentException(String.format("can't find setter method for field '%s'", field.getName()));
        }

        Method getterMethod = ClassUtils.getterMethod(field);
        if (Objects.isNull(getterMethod)) {
            throw new IllegalArgumentException(String.format("can't find getter method for field '%s'", field.getName()));
        }

        int hashcode = field.hashCode();
        String suffix = hashcode > 0 ? "1" + hashcode : "0" + -hashcode;
        Class<? extends BiFunction> biFuncClass = new ByteBuddy()
                .subclass(BiFunction.class)
                .name("BiFunction" + suffix)
                .method(ElementMatchers.named("apply"))
                .intercept(MethodCall.invoke(setterMethod).onArgument(0).withArgument(1).withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                .make()
                .load(ByteBuddyField.class.getClassLoader())
                .getLoaded();
        setter = ClassUtils.instance(biFuncClass);

        Class<? extends Function> funcClass = new ByteBuddy()
                .subclass(Function.class)
                .name("Function" + suffix)
                .method(ElementMatchers.named("apply"))
                .intercept(MethodCall.invoke(getterMethod).onArgument(0).withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                .make()
                .load(ByteBuddyField.class.getClassLoader())
                .getLoaded();
        getter = ClassUtils.instance(funcClass);
    }

    @Override
    protected void merge0(Input input, Object message) {
        Object value = afterRead(Runtime.read(input, schema));
        setter.apply(message, value);
    }

    @Override
    protected void write0(Output output, Object message) {
        Object value = beforeWrite(getter.apply(message));
        Runtime.write(output, value, schema);
    }
}
