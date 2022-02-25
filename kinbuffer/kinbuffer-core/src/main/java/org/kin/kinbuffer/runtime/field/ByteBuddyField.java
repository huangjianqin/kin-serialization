package org.kin.kinbuffer.runtime.field;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatchers;
import org.kin.framework.utils.ClassUtils;
import org.kin.kinbuffer.runtime.Schema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 基于bytebuddy的field处理
 *
 * @author huangjianqin
 * @date 2021/12/19
 */
@SuppressWarnings({"rawtypes"})
public class ByteBuddyField extends EnhanceField {

    public ByteBuddyField(java.lang.reflect.Field field) {
        this(field, null);
    }

    public ByteBuddyField(java.lang.reflect.Field field, Schema schema) {
        super(field, schema);
    }

    @Override
    protected BiConsumer genSetter(Field field) {
        //创建setter代理方法
        Method setterMethod = ClassUtils.setterMethod(field);
        if (Objects.isNull(setterMethod)) {
            throw new IllegalArgumentException(String.format("can't find setter method for field '%s'", field.getName()));
        }

        int hashcode = field.hashCode();
        //处理负数的情况
        String suffix = hashcode > 0 ? "1" + hashcode : "0" + -hashcode;
        Class<? extends BiConsumer> biFuncClass = new ByteBuddy()
                .subclass(BiConsumer.class)
                .name("BiConsumer" + suffix)
                .method(ElementMatchers.named("accept"))
                .intercept(MethodCall.invoke(setterMethod).onArgument(0).withArgument(1).withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                .make()
                .load(ByteBuddyField.class.getClassLoader())
                .getLoaded();
        return ClassUtils.instance(biFuncClass);
    }

    @Override
    protected Function genGetter(Field field) {
        //创建getter代理方法
        Method getterMethod = ClassUtils.getterMethod(field);
        if (Objects.isNull(getterMethod)) {
            throw new IllegalArgumentException(String.format("can't find getter method for field '%s'", field.getName()));
        }

        int hashcode = field.hashCode();
        //处理负数的情况
        String suffix = hashcode > 0 ? "1" + hashcode : "0" + -hashcode;

        Class<? extends Function> funcClass = new ByteBuddy()
                .subclass(Function.class)
                .name("Function" + suffix)
                .method(ElementMatchers.named("apply"))
                .intercept(MethodCall.invoke(getterMethod).onArgument(0).withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                .make()
                .load(ByteBuddyField.class.getClassLoader())
                .getLoaded();
        return ClassUtils.instance(funcClass);
    }
}
