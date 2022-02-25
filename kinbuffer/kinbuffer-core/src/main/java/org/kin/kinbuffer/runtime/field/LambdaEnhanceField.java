package org.kin.kinbuffer.runtime.field;

import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.kinbuffer.runtime.Schema;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 基于{@link LambdaMetafactory}的field处理
 * 相比bytebuddy, 生成schema和write的性能更佳
 * @author huangjianqin
 * @date 2022/2/25
 */
@SuppressWarnings({"rawtypes"})
public final class LambdaEnhanceField extends EnhanceField {
    public LambdaEnhanceField(java.lang.reflect.Field field) {
        this(field, null);
    }

    public LambdaEnhanceField(java.lang.reflect.Field field, Schema schema) {
        super(field, schema);
    }

    /**
     * 获取field type实例类型, 会把对应的primitive类型转换成包装类型
     */
    private Class<?> getFiledObjectType(Field field) {
        Class<?> fieldType = field.getType();
        //primitive处理
        if (Boolean.TYPE.equals(fieldType)) {
            fieldType = Boolean.class;
        } else if (Byte.TYPE.equals(fieldType)) {
            fieldType = Byte.class;
        } else if (Character.TYPE.equals(fieldType)) {
            fieldType = Character.class;
        } else if (Short.TYPE.equals(fieldType)) {
            fieldType = Short.class;
        } else if (Integer.TYPE.equals(fieldType)) {
            fieldType = Integer.class;
        } else if (Long.TYPE.equals(fieldType)) {
            fieldType = Long.class;
        } else if (Float.TYPE.equals(fieldType)) {
            fieldType = Float.class;
        } else if (Double.TYPE.equals(fieldType)) {
            fieldType = Double.class;
        }
        return fieldType;
    }

    @Override
    protected BiConsumer genSetter(Field field) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            //创建setter代理方法
            Method setterMethod = ClassUtils.setterMethod(field);
            if (Objects.isNull(setterMethod)) {
                throw new IllegalArgumentException(String.format("can't find setter method for field '%s'", field.getName()));
            }

            Class<?> fieldType = getFiledObjectType(field);

            MethodHandle setterMethodHandle = lookup.unreflect(setterMethod);
            return (BiConsumer) LambdaMetafactory.metafactory(lookup, "accept",
                    MethodType.methodType(BiConsumer.class),
                    MethodType.methodType(void.class, field.getDeclaringClass(), fieldType).erase(),
                    setterMethodHandle,
                    MethodType.methodType(void.class, field.getDeclaringClass(), fieldType)
            ).getTarget().invoke();
        } catch (Throwable e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        return null;
    }

    @Override
    protected Function genGetter(Field field) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            //创建getter代理方法
            Method getterMethod = ClassUtils.getterMethod(field);
            if (Objects.isNull(getterMethod)) {
                throw new IllegalArgumentException(String.format("can't find getter method for field '%s'", field.getName()));
            }

            Class<?> fieldType = getFiledObjectType(field);

            MethodHandle getterMethodHandle = lookup.unreflect(getterMethod);
            return (Function) LambdaMetafactory.metafactory(lookup, "apply",
                    MethodType.methodType(Function.class),
                    MethodType.methodType(fieldType, field.getDeclaringClass()).erase(),
                    getterMethodHandle,
                    MethodType.methodType(fieldType, field.getDeclaringClass())
            ).getTarget().invoke();
        } catch (Throwable e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        return null;
    }
}
