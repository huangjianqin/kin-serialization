package org.kin.kinbuffer.runtime.field;

import org.eclipse.collections.api.map.primitive.IntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.api.map.primitive.ObjectIntMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.eclipse.collections.impl.factory.primitive.ObjectIntMaps;
import org.kin.framework.collection.CopyOnWriteMap;
import org.kin.framework.utils.CollectionUtils;
import org.kin.framework.utils.VarIntUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.*;
import org.kin.kinbuffer.runtime.Runtime;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;

/**
 * 字段类型是对象
 *
 * @author huangjianqin
 * @date 2022/4/21
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class ObjectField extends Field {
    /** non null flag, 第8个bit为1 */
    private static final byte NON_NULL_FLAG = (byte) 0x80;
    /** 泛化code bit数, 5bit, 即1-31 */
    private static final byte GENERIC_CODE_SHIFT = 2;
    /** 泛化code, 5bit, 即1-31 */
    private static final byte GENERIC_CODE_MASK = (byte) 0x7C;

    /** 该字段{@link java.lang.reflect.Field}对应类型的{@link Schema}实例, null则表示pojo, 需lazy init */
    @Nullable
    protected Schema schema;
    protected final boolean deprecated;
    /** 字段值是否可能为null */
    protected final boolean optional;
    /** key -> 泛化类型对应序列化的code, value -> 泛化类型 */
    protected final IntObjectMap<Class<?>> code2GenericClass;
    /** key -> 泛化类型, value -> 泛化类型对应序列化的code */
    protected final ObjectIntMap<Class<?>> genericClass2Code;
    /** key -> 泛化类型, value -> 泛化类型对应{@link Schema}实现类 */
    protected final Map<Class<?>, Schema> genericSchemaMap = new CopyOnWriteMap<>();

    protected ObjectField(java.lang.reflect.Field field) {
        this(field, null);
    }

    protected ObjectField(java.lang.reflect.Field field, @Nullable Schema schema) {
        super(field);
        this.schema = schema;

        this.deprecated = field.isAnnotationPresent(Deprecated.class);
        if (isDeprecated() && this.since == VersionUtils.MIN_VERSION) {
            throw new IllegalStateException("@Deprecated must be with @Since together");
        }

        this.optional = field.isAnnotationPresent(Optional.class);
        if (optional && type.isPrimitive()) {
            throw new IllegalStateException("@Optional just support on object type, not primitive type");
        }

        UseGeneric useGeneric = field.getAnnotation(UseGeneric.class);
        if (Objects.nonNull(useGeneric)) {
            Generic generic = (Generic) type.getAnnotation(Generic.class);
            if (Objects.nonNull(generic)) {
                MutableIntObjectMap<Class<?>> code2GenericClass = IntObjectMaps.mutable.of();
                MutableObjectIntMap<Class<?>> genericClass2Code = ObjectIntMaps.mutable.of();
                GenericType[] genericTypes = generic.types();
                if(CollectionUtils.isNonEmpty(genericTypes)){
                    for (GenericType genericType : genericTypes) {
                        int genericCode = genericType.code();
                        Class<?> genericType1 = genericType.type();
                        if(genericCode <= 0 || genericCode > GENERIC_CODE_MASK){
                            throw new IllegalArgumentException(String.format("generic code for type '%s' must be in [%d,%d]",
                                    genericType1.getName(), 1, GENERIC_CODE_MASK));
                        }
                        code2GenericClass.put(genericCode, genericType1);
                        genericClass2Code.put(genericType1, genericCode);
                    }

                    this.code2GenericClass = IntObjectMaps.immutable.ofAll(code2GenericClass);
                    this.genericClass2Code = ObjectIntMaps.immutable.ofAll(genericClass2Code);
                }
                else{
                    throw new IllegalStateException("field type '%s' is annotated with @Generic but its types is empty when use generic");
                }
            }
            else{
                throw new IllegalStateException("field type '%s' is not annotated with @Generic when use generic");
            }
        } else {
            code2GenericClass = IntObjectMaps.immutable.empty();
            genericClass2Code = ObjectIntMaps.immutable.empty();
        }
    }

    /**
     * lazy init schema
     */
    private Schema getOrInitSchema() {
        if (Objects.isNull(schema)) {
            schema = Runtime.getSchema(type);
        }

        return schema;
    }

    /**
     * lazy init generic schema
     */
    private Schema getOrInitGenericSchema(Class<?> type) {
        if (!genericSchemaMap.containsKey(type)) {
            Schema schema = Runtime.getSchema(type);
            genericSchemaMap.put(type, schema);
            return schema;
        }

        return genericSchemaMap.get(type);
    }

    @Override
    public final void merge(Input input, Object message) {
        boolean canMerge = true;
        int genericCode = 0;
        if(isOptional() || withGeneric()){
            int flag = input.readByte();
            genericCode = (flag & GENERIC_CODE_MASK) >> GENERIC_CODE_SHIFT;
            if(((byte) flag & NON_NULL_FLAG) == 0){
                //即为null
                canMerge = false;
            }
        }

        if(canMerge){
            Schema schema;
            if(genericCode > 0){
                Class<?> actualType = code2GenericClass.get(genericCode);
                schema = getOrInitGenericSchema(actualType);
            }
            else{
                schema = getOrInitSchema();
            }
            set(message, SchemaUtils.read(input, schema));
        }
    }

    @Override
    public final void write(Output output, Object message) {
        if(isDeprecated()){
            return;
        }

        Object value = get(message);
        boolean nonNull = Objects.nonNull(value);
        if(!nonNull && !isOptional()){
            throw new IllegalArgumentException(String.format("field '%s' of %s is null, but it is not annotated with @Optional",
                    field.getName(), field.getDeclaringClass().getName()));
        }

        int genericCode = 0;
        Schema schema = null;
        if(nonNull){
            if(withGeneric()){
                //带泛化类型
                Class<?> actualType = value.getClass();
                genericCode = genericClass2Code.get(actualType);
                if(genericCode == 0){
                    throw new IllegalStateException(String.format("unknown generic code for class '%s'", actualType.getName()));
                }
                schema = getOrInitGenericSchema(actualType);
            }
            else{
                //pojo
                schema = getOrInitSchema();
            }
        }

        if(isOptional() || genericCode > 0){
            //字段值可能为null, 那么需要写入一个byte标识是否为非null
            writeFlag(output, nonNull, genericCode);
        }

        if (nonNull) {
            SchemaUtils.write(output, value, schema);
        }
    }

    /**
     * 组装写object的flag, 压缩字节流
     */
    private void writeFlag(Output output, boolean nonNull, int genericCode){
        int flag = (nonNull ? NON_NULL_FLAG : 0) + (genericCode << GENERIC_CODE_SHIFT);
        output.writeByte(flag);
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

    public boolean isOptional() {
        return optional;
    }

    public boolean withGeneric(){
        return !code2GenericClass.isEmpty() && !genericClass2Code.isEmpty();
    }

    public Class<?> getGenericClass(int code) {
        return code2GenericClass.get(code);
    }

    public int getGenericClassCode(Class<?> genericClass) {
        return genericClass2Code.get(genericClass);
    }

    @Override
    public boolean isDeprecated() {
        return deprecated;
    }
}
