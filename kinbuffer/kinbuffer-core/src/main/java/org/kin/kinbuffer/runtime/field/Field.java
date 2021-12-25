package org.kin.kinbuffer.runtime.field;

import org.kin.framework.utils.VarIntUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.Runtime;
import org.kin.kinbuffer.runtime.Schema;
import org.kin.kinbuffer.runtime.Signed;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 类成员字段逻辑处理, 用于处理字段赋值与取值
 *
 * @author huangjianqin
 * @date 2021/12/19
 */
@SuppressWarnings("rawtypes")
public abstract class Field {
    /** 非整形 */
    private static final byte NONE = 0;
    /** 无符号整形32 */
    private static final byte UNSIGNED_INT32 = 1;
    /** 无符号整形64 */
    private static final byte UNSIGNED_INT64 = 2;
    /** 有符号整形32 */
    private static final byte SIGNED_INT32 = 3;
    /** 有符号整形64 */
    private static final byte SIGNED_INT64 = 4;

    private final java.lang.reflect.Field field;
    /** 该字段{@link java.lang.reflect.Field}对应类型 */
    protected final Class type;
    /** 该字段{@link java.lang.reflect.Field}对应类型的{@link Schema}实例, null则表示pojo, 需lazy init */
    @Nullable
    protected Schema schema;
    /** 字段类型, 用于标识是否是有符号整形, 则使用zigzag */
    protected final byte intType;

    protected Field(java.lang.reflect.Field field) {
        this(field, null);
    }

    protected Field(java.lang.reflect.Field field, Schema schema) {
        this.field = field;
        this.type = field.getType();
        this.schema = schema;

        //是否是有符号整形
        boolean signed = field.isAnnotationPresent(Signed.class);
        if (Integer.class.equals(type) || Integer.TYPE.equals(type)) {
            if (signed) {
                intType = SIGNED_INT32;
            } else {
                intType = UNSIGNED_INT32;
            }
        } else if (Long.class.equals(type) || Long.TYPE.equals(type)) {
            if (signed) {
                intType = SIGNED_INT64;
            } else {
                intType = UNSIGNED_INT64;
            }
        } else {
            intType = NONE;
        }
    }

    /**
     * lazy init schema
     */
    private void tryLazyInitSchema(){
        if (Objects.isNull(schema)) {
            schema = Runtime.getSchema(type);
        }
    }

    /**
     * 从{@code input}读取bytes, 并给{@code message}相应字段赋值
     *
     * @param message 消息实例, 读取字段值并赋值给消息
     */
    public void merge(Input input, Object message){
        tryLazyInitSchema();
        merge0(input, message);
    }

    /**
     * 将{@code message}实例所有字段转换成bytes, 写出到{@code output}
     *
     * @param message 消息实例, 从消息读取字段值并写出
     */
    public void write(Output output, Object message){
        tryLazyInitSchema();
        write0(output, message);
    }


    /**
     * 从{@code input}读取bytes, 并给{@code message}相应字段赋值
     *
     * @param message 消息实例, 读取字段值并赋值给消息
     */
    protected abstract void merge0(Input input, Object message);

    /**
     * 将{@code message}实例所有字段转换成bytes, 写出到{@code output}
     *
     * @param message 消息实例, 从消息读取字段值并写出
     */
    protected abstract void write0(Output output, Object message);

    /**
     * write output之前对value自定义逻辑处理
     */
    protected final Object beforeWrite(Object target) {
        if (intType == SIGNED_INT32) {
            //对有符号32位整形进行zigzag编码
            return VarIntUtils.encodeZigZag32((int) target);
        } else if (intType == SIGNED_INT64) {
            //对有符号64位整形进行zigzag编码
            return VarIntUtils.encodeZigZag64((long) target);
        } else {
            return target;
        }
    }

    /**
     * 从input read之后对value自定义逻辑处理
     */
    protected final Object afterRead(Object target) {
        if (intType == SIGNED_INT32) {
            //对有符号32位整形进行zigzag解码
            return VarIntUtils.decodeZigZag32((int) target);
        } else if (intType == SIGNED_INT64) {
            //对有符号64位整形进行zigzag解码
            return VarIntUtils.decodeZigZag64((long) target);
        } else {
            return target;
        }
    }

    //getter

    public java.lang.reflect.Field getField() {
        return field;
    }

    public Class getType() {
        return type;
    }

    @Nullable
    public Schema getSchema() {
        return schema;
    }

    public byte getIntType() {
        return intType;
    }
}