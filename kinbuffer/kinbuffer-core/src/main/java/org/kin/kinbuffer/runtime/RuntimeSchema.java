package org.kin.kinbuffer.runtime;

import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.field.Field;
import org.kin.kinbuffer.runtime.field.PrimitiveUnsafeField;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2021/12/11
 */
final class RuntimeSchema<T> implements Schema<T> {
    /** pojo类型 */
    private final Class<T> typeClass;
    /** {@link Field}实现 */
    private final List<org.kin.kinbuffer.runtime.field.Field> fields;
    /** 该pojo constructor */
    private final Constructor<T> constructor;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public RuntimeSchema(Class typeClass, List<org.kin.kinbuffer.runtime.field.Field> fields) {
        this.typeClass = typeClass;
        this.fields = fields;

        //适用于于反序列化的构造器, 不会初始化对象
        Constructor<T> constructor = ClassUtils.getNotLoadConstructor(typeClass);
        if (Objects.isNull(constructor)) {
            //兜底, 获取无参构造器
            try {
                constructor = typeClass.getConstructor();
            } catch (NoSuchMethodException e) {
                ExceptionUtils.throwExt(e);
            }
        }

        this.constructor = constructor;
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
        for (Field field : fields) {
            boolean nonNull;
            if (field instanceof PrimitiveUnsafeField) {
                //primitive, 不存在null, 直接读
                nonNull = true;
            } else {
                //object
                nonNull = input.readBoolean();
            }

            if(nonNull){
                //read from input and set field value
                field.merge(input, message);
            }
        }
    }

    @Override
    public void write(Output output, T message) {
        for (Field field : fields) {
            //write field value to output
            field.write(output, message);
        }
    }
}
