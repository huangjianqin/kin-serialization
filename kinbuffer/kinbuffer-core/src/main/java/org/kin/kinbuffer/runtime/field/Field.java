package org.kin.kinbuffer.runtime.field;

import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.api.map.primitive.ObjectIntMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.eclipse.collections.impl.factory.primitive.ObjectIntMaps;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.*;

import java.util.Objects;

/**
 * 类成员字段逻辑处理, 用于处理字段赋值与取值
 *
 * @author huangjianqin
 * @date 2021/12/19
 */
@SuppressWarnings("rawtypes")
public abstract class Field {
    protected final java.lang.reflect.Field field;
    /** 该字段{@link java.lang.reflect.Field}对应类型 */
    protected final Class type;
    /** 用于标识是否是有符号整形, 则使用zigzag */
    protected final boolean signed;
    protected final int since;

    protected Field(java.lang.reflect.Field field) {
        this.field = field;
        this.type = field.getType();
        this.signed = field.isAnnotationPresent(Signed.class);
        Since sinceAnno = field.getAnnotation(Since.class);
        if (Objects.nonNull(sinceAnno)) {
            this.since = sinceAnno.value();
            if (this.since <= VersionUtils.MIN_VERSION) {
                throw new IllegalStateException("@Since version must be greater than " + VersionUtils.MIN_VERSION);
            }
        } else {
            this.since = VersionUtils.MIN_VERSION;
        }

        VersionUtils.checkVersion(this.since);
    }

    /**
     * 从{@code input}读取bytes, 并给{@code message}相应字段赋值
     *
     * @param message 消息实例, 读取字段值并赋值给消息
     */
    public abstract void merge(Input input, Object message);

    /**
     * 从{@code message}实例获取指定字段值, 并写入{@code output}
     *
     * @param message 消息实例, 从消息读取字段值并写出output
     */
    public abstract void write(Output output, Object message);

    /**
     * 判断该字段是否在指定版本{@code version}后定义
     */
    public boolean isSince(int version) {
        return since > version;
    }

    //getter
    public java.lang.reflect.Field getField() {
        return field;
    }

    public Class getType() {
        return type;
    }

    public boolean isSigned() {
        return signed;
    }

    public boolean isDeprecated() {
        throw new UnsupportedOperationException("field is not support to annotated with @Deprecated");
    }

    public int getSince() {
        return since;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Field)) {
            return false;
        }
        Field field1 = (Field) o;
        return Objects.equals(field, field1.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field);
    }
}