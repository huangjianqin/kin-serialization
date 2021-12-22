package org.kin.kinbuffer.schema.field;

import org.kin.framework.utils.VarIntUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.schema.Schema;
import org.kin.kinbuffer.schema.Signed;

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

    /** 该字段{@link java.lang.reflect.Field}对应类型 */
    protected final Class type;
    /** 该字段{@link java.lang.reflect.Field}对应类型的{@link Schema}实例 */
    protected final Schema schema;
    /** 整形类型 */
    protected final byte intType;

    protected Field(java.lang.reflect.Field field, Schema schema) {
        this.type = field.getType();
        this.schema = schema;

        //是否是有符号整形
        boolean signed = field.isAnnotationPresent(Signed.class);
        if (Short.class.equals(type) || Short.TYPE.equals(type) ||
                Integer.class.equals(type) || Integer.TYPE.equals(type)) {
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
     * 从{@code input}读取bytes, 并给{@code message}相应字段赋值
     *
     * @param message 消息实例, 读取字段值并赋值给消息
     */
    public abstract void merge(Input input, Object message);

    /**
     * 将{@code message}实例所有字段转换成bytes, 写出到{@code output}
     *
     * @param message 消息实例, 从消息读取字段值并写出
     */
    public abstract void write(Output output, Object message);

    /**
     * write output之前对value自定义逻辑处理
     */
    protected Object beforeWrite(Object target) {
        if (intType == SIGNED_INT32) {
            //对有符号32位整形进行zigzag编码
            int int32;
            if(Short.class.equals(type) || Short.TYPE.equals(type)) {
                int32 = ((short) target);
            }
            else{
                int32 = ((int) target);
            }
            return VarIntUtils.encodeZigZag32(int32);
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
    protected Object afterRead(Object target) {
        if (intType == SIGNED_INT32) {
            //对有符号32位整形进行zigzag解码
            if(Short.class.equals(type) || Short.TYPE.equals(type)) {
                return (short)VarIntUtils.decodeZigZag32((short) target);
            }
            else{
                return VarIntUtils.decodeZigZag32((int) target);
            }
        } else if (intType == SIGNED_INT64) {
            //对有符号64位整形进行zigzag解码
            return VarIntUtils.decodeZigZag64((long) target);
        } else {
            return target;
        }
    }
}