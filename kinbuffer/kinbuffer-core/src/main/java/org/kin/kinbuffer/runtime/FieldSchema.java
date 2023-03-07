package org.kin.kinbuffer.runtime;

import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.field.Field;

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
        int version = input.readVarInt32();
        for (Field field : fields) {
            if(field.isSince(version)){
                if(!field.isDeprecated()){
                    //后面版本新加的字段, 过滤
                    continue;
                }
                //后面版本新加的@Deprecated, 照样尝试读取
            }
            //read from input and set field value
            field.merge(input, message);
        }
    }

    @Override
    public void write(Output output, T message) {
        output.writeVarInt32(this.version);
        for (Field field : fields) {
            //write field value to output
            field.write(output, message);
        }
    }
}
