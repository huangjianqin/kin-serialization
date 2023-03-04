package org.kin.kinbuffer.runtime.field;

import org.kin.framework.utils.VarIntUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.Runtime;
import org.kin.kinbuffer.runtime.Schema;
import org.kin.kinbuffer.runtime.SchemaUtils;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 字段类型是对象
 *
 * @author huangjianqin
 * @date 2022/4/21
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class ObjectField extends Field {
    /** 该字段{@link java.lang.reflect.Field}对应类型的{@link Schema}实例, null则表示pojo, 需lazy init */
    @Nullable
    protected Schema schema;

    protected ObjectField(java.lang.reflect.Field field) {
        super(field);
    }

    protected ObjectField(java.lang.reflect.Field field, @Nullable Schema schema) {
        super(field);
        this.schema = schema;
    }

    /**
     * lazy init schema
     */
    private void tryLazyInitSchema() {
        if (Objects.isNull(schema)) {
            schema = Runtime.getSchema(type);
        }
    }

    @Override
    public final void merge(Input input, Object message) {
        tryLazyInitSchema();
        boolean canMerge = true;
        // TODO: 2023/3/2 以后有什么flag字段可以复用这个byte, 进而省流
        if(isOptional()){
            boolean nonNull = input.readBoolean();
            if(!nonNull){
                canMerge = false;
            }
        }

        if(canMerge){
            set(message, SchemaUtils.read(input, schema));
        }
    }


    @Override
    public final void write(Output output, Object message) {
        if(isDeprecated()){
            return;
        }

        tryLazyInitSchema();
        Object value = get(message);
        boolean nonNull = Objects.nonNull(value);
        if(!nonNull && !isOptional()){
            throw new IllegalArgumentException(String.format("field '%s' of %s is null, but it is not annotated with @Optional",
                    field.getName(), field.getDeclaringClass().getName()));
        }
        if(isOptional()){
            //字段值可能为null, 那么需要写入一个byte标识是否为非null
            output.writeBoolean(nonNull);
        }
        if (nonNull) {
            SchemaUtils.write(output, value, schema);
        }
    }

    /**
     * 从input read之后对value自定义逻辑处理
     */
    protected final Object afterRead(Object target) {
        if (Objects.isNull(target)) {
            return null;
        }

        if(isSigned()){
            if(Byte.class.equals(type)){
                //对有符号32位整形进行zigzag解码
                return (byte)VarIntUtils.decodeZigZag32((Byte) target);
            }else if(Short.class.equals(type)){
                //对有符号32位整形进行zigzag解码
                return (short)VarIntUtils.decodeZigZag32((Short) target);
            } else if(Integer.class.equals(type)){
                //对有符号32位整形进行zigzag解码
                return VarIntUtils.decodeZigZag32((Integer) target);
            }else if (Long.class.equals(type)) {
                //对有符号64位整形进行zigzag解码
                return VarIntUtils.decodeZigZag64((Long) target);
            }
        }

        return target;
    }

    /**
     * write output之前对value自定义逻辑处理
     */
    protected final Object beforeWrite(Object target) {
        if (Objects.isNull(target)) {
            return null;
        }

        if(isSigned()){
            if(Byte.class.equals(type)){
                //对有符号32位整形进行zigzag编码
                return (byte)VarIntUtils.encodeZigZag32((Byte) target);
            }else if(Short.class.equals(type)){
                //对有符号32位整形进行zigzag编码
                return (short)VarIntUtils.encodeZigZag32((Short) target);
            } else if(Integer.class.equals(type)){
                //对有符号32位整形进行zigzag编码
                return VarIntUtils.encodeZigZag32((Integer) target);
            }else if (Long.class.equals(type)) {
                //对有符号64位整形进行zigzag编码
                return VarIntUtils.encodeZigZag64((Long) target);
            }
        }

        return target;
    }

    /**
     * 给{@code message}相应字段赋值
     *
     * @param message  消息实例, 读取字段值并赋值给消息
     * @param rawValue 从input读取出来的消息, 没有加工过
     */
    protected abstract void set(Object message, Object rawValue);

    /**
     * 从{@code message}实例获取指定字段值
     *
     * @param message 消息实例, 从消息读取字段值并写出
     */
    protected abstract Object get(Object message);

    //getter
    @Nullable
    public Schema getSchema() {
        return schema;
    }
}
