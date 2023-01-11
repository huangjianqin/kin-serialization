package org.kin.kinbuffer.runtime;

import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.field.Field;
import org.kin.kinbuffer.runtime.field.ObjectField;
import org.kin.kinbuffer.runtime.field.PrimitiveUnsafeField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

/**
 * 基于类field字段定义顺序实现序列化和反序列化逻辑
 * @author huangjianqin
 * @date 2021/12/11
 */
final class FieldSchema<T> implements Schema<T> {
    /** pojo类型 */
    private final Class<T> typeClass;
    /** {@link Field}实现 */
    private final List<org.kin.kinbuffer.runtime.field.Field> fields;
    /** 该pojo constructor */
    private final Constructor<T> constructor;
    /** type class version */
    private final int version;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public FieldSchema(Class typeClass, List<org.kin.kinbuffer.runtime.field.Field> fields) {
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
        Version versionAnno = (Version) typeClass.getAnnotation(Version.class);
        if (Objects.nonNull(versionAnno)) {
            this.version = versionAnno.value();
        }
        else{
            this.version = VersionUtils.MIN_VERSION;
        }

        VersionUtils.checkVersion(this.version);
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
        int version = input.readInt32();
        for (Field field : fields) {
            if(field.isSince(version)){
                continue;
            }
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
        output.writeInt32(this.version);
        for (Field field : fields) {
            //write field value to output
            if(field instanceof ObjectField && field.isDeprecated()){
                //deprecated, 强制写入null对象
                output.writeBoolean(false);
            }
            else{
                field.write(output, message);
            }
        }
    }
}
