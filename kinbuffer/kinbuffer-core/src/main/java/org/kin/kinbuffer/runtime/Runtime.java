package org.kin.kinbuffer.runtime;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import org.kin.framework.utils.ClassScanUtils;
import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.UnsafeUtil;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.field.ByteBuddyField;
import org.kin.kinbuffer.runtime.field.ReflectionField;
import org.kin.kinbuffer.runtime.field.UnsafeField;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author huangjianqin
 * @date 2021/12/19
 */
@SuppressWarnings({"rawtypes", "unchecked", "ConstantConditions"})
public class Runtime {
    /** 使用支持字节码增强 */
    public static final boolean ENHANCE;

    /** 基于copy-on-write更新, 以提高读性能 todo 是否可以以hashcode为key */
    private static volatile Map<String, Schema> schemas = new HashMap<>();
    /** todo 是否考虑动态扩容 */
    private static final BiMap<Integer, Class> idClassMap;

    static {
        Class<?> byteBuddyClass = null;
        try {
            byteBuddyClass = Class.forName("net.bytebuddy.ByteBuddy");
        } catch (Exception e) {
            //ignore
        }

        ENHANCE = Objects.nonNull(byteBuddyClass);

        schemas.put(String.class.getName(), StringSchema.INSTANCE);
        schemas.put(Boolean.class.getName(), BooleanSchema.INSTANCE);
        schemas.put(Boolean.TYPE.getName(), BooleanSchema.INSTANCE);
        schemas.put(Byte.class.getName(), ByteSchema.INSTANCE);
        schemas.put(Byte.TYPE.getName(), ByteSchema.INSTANCE);
        schemas.put(Character.class.getName(), CharacterSchema.INSTANCE);
        schemas.put(Character.TYPE.getName(), CharacterSchema.INSTANCE);
        schemas.put(Short.class.getName(), ShortSchema.INSTANCE);
        schemas.put(Short.TYPE.getName(), ShortSchema.INSTANCE);
        schemas.put(Integer.class.getName(), IntegerSchema.INSTANCE);
        schemas.put(Integer.TYPE.getName(), IntegerSchema.INSTANCE);
        schemas.put(Long.class.getName(), LongSchema.INSTANCE);
        schemas.put(Long.TYPE.getName(), LongSchema.INSTANCE);
        schemas.put(Float.class.getName(), FloatSchema.INSTANCE);
        schemas.put(Float.TYPE.getName(), FloatSchema.INSTANCE);
        schemas.put(Double.class.getName(), DoubleSchema.INSTANCE);
        schemas.put(Double.TYPE.getName(), DoubleSchema.INSTANCE);

        //扫描所有带@MessageId注解的class, 并注册
        Set<Integer> messageIds = new HashSet<>();
        ImmutableBiMap.Builder<Integer, Class> idClassMapBuilder = ImmutableBiMap.builder();
        for (Class<?> claxx : ClassScanUtils.scan(MessageId.class)) {
            MessageId messageId = claxx.getAnnotation(MessageId.class);
            int id = messageId.id();
            if (id <= 0) {
                throw new IllegalStateException(String.format("message id must be greater than zero, id '%d', class '%s'", id, claxx.getCanonicalName()));
            }
            if (!messageIds.add(id)) {
                throw new IllegalStateException(String.format("duplication message id '%d', class '%s'", id, claxx.getCanonicalName()));
            }
            idClassMapBuilder.put(id, claxx);
        }
        idClassMap = idClassMapBuilder.build();
    }

    /**
     * for test, 用于预初始化{@link Runtime}(static块逻辑), 测试时, 过滤初始化的性能消耗
     */
    public static void load(){
        //do nothing
    }

    public static Object read(Input input, Schema schema) {
        if (schema instanceof PolymorphicSchema) {
            return ((PolymorphicSchema) schema).read(input);
        } else {
            Object message = schema.newMessage();
            schema.merge(input, message);
            return message;
        }
    }

    public static void write(Output output, Object target, Schema schema) {
        if (Objects.isNull(schema)) {
            Class typeClass = target.getClass();
            getSchema(typeClass).write(output, target);
        } else {
            schema.write(output, target);
        }
    }

    /**
     * 获取{@code typeClass}的{@link Schema}实现
     */
    public static <T> Schema<T> getSchema(Class<T> typeClass) {
        Schema<T> schema = schemas.get(typeClass.getName());
        if (Objects.isNull(schema)) {
            schema = constructSchema(typeClass);
        }
        return schema;
    }

    private static synchronized <T> Schema<T> constructSchema(Class<T> typeClass) {
        Schema<T> schema = constructSchema0(typeClass);

        Map<String, Schema> schemas = new HashMap<>(Runtime.schemas);
        schemas.put(typeClass.getName(), schema);
        Runtime.schemas = schemas;

        return schema;
    }

    private static <T> Schema<T> constructSchema0(Class<T> typeClass) {
        if (typeClass.isEnum()) {
            return (Schema<T>) new EnumSchema<>((Class<? extends Enum>) typeClass);
        } else {
            return constructPoJoSchema0(typeClass);
        }
    }

    private static <T> Schema<T> constructPoJoSchema0(Class<T> typeClass) {
        List<Field> allFields = ClassUtils.getAllFields(typeClass);
        List<org.kin.kinbuffer.runtime.field.Field> fields = new ArrayList<>(allFields.size());
        for (java.lang.reflect.Field field : allFields) {
            int modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers) ||
                    Modifier.isStatic(modifiers) ||
                    Modifier.isTransient(modifiers)) {
                //final or static or transient
                continue;
            }

            if (field.isAnnotationPresent(Deprecated.class)) {
                //无用字段
                continue;
            }

            Class<?> type = field.getType();
            if (type.isAnnotation()) {
                //注解
                continue;
            }

            Schema schema = null;
            if (Collection.class.isAssignableFrom(type)) {
                schema = getCollectionSchema(field.getGenericType());
            } else if (Map.class.isAssignableFrom(type)) {
                schema = getMapSchema(field.getGenericType());
            } else if (type.isArray()) {
                schema = getArraySchema(type);
            } else {
                //primitive or pojo
            }

            if (ENHANCE) {
                fields.add(new ByteBuddyField(field, schema));
            } else {
                if (UnsafeUtil.hasUnsafe()) {
                    fields.add(new UnsafeField(field, schema));
                } else {
                    fields.add(new ReflectionField(field, schema));
                }
            }
        }

        return new RuntimeSchema<>(typeClass, fields);
    }

    private static Schema getArraySchema(Class type) {
        Class componentType = type.getComponentType();

        if (Collection.class.isAssignableFrom(componentType)) {
            //嵌套collection
            return new MessageArraySchema(componentType, getCollectionSchema(componentType));
        } else if (Map.class.isAssignableFrom(componentType)) {
            //嵌套map
            return new MessageArraySchema(componentType, getMapSchema(componentType));
        } else if (componentType.isArray()) {
            //嵌套array
            return new MessageArraySchema(componentType, getArraySchema(componentType));
        } else if (Object.class.equals(componentType)) {
            // TODO: 2021/12/19 没有使用泛型, item类型为Object, 得动态处理
            return null;
        } else {
            //primitive or pojo
            return new MessageArraySchema(componentType);
        }
    }

    private static Schema getCollectionSchema(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type actualType = pt.getActualTypeArguments()[0];
            CollectionFactory collectionFactory = CollectionFactory.getFactory((Class<? extends Collection<?>>) pt.getRawType());

            if (actualType instanceof ParameterizedType) {
                ParameterizedType apt = (ParameterizedType) actualType;
                Class ac = (Class) apt.getRawType();

                if (Collection.class.isAssignableFrom(ac)) {
                    //嵌套collection
                    return new MessageCollectionSchema(collectionFactory, ac, getCollectionSchema(actualType));
                } else if (Map.class.isAssignableFrom(ac)) {
                    //嵌套map
                    return new MessageCollectionSchema(collectionFactory, ac, getMapSchema(actualType));
                } else {
                    //primitive or pojo
                    return new MessageCollectionSchema<>(collectionFactory, ac);
                }
            } else {
                Class ac = (Class) actualType;
                if (ac.isArray()) {
                    //嵌套array
                    return new MessageCollectionSchema(collectionFactory, ac, getArraySchema(ac));
                } else if (Object.class.equals(ac)) {
                    // TODO: 2021/12/19 没有使用泛型, item类型为Object, 得动态处理
                    return null;
                } else {
                    //primitive or pojo
                    return new MessageCollectionSchema<>(collectionFactory, ac);
                }
            }
        } else {
            // TODO: 2021/12/19 没有使用泛型, item类型为Object, 得动态处理
            return null;
        }
    }

    private static Schema getMapSchema(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type actualKeyType = pt.getActualTypeArguments()[0];
            Type actualValueType = pt.getActualTypeArguments()[1];
            MapFactory mapFactory = MapFactory.getFactory((Class<? extends Map<?, ?>>) pt.getRawType());

            Schema keySchema = null;
            Class keyClass = null;
            if (actualKeyType instanceof ParameterizedType) {
                ParameterizedType apt = (ParameterizedType) actualKeyType;
                keyClass = (Class) apt.getRawType();

                if (Collection.class.isAssignableFrom(keyClass)) {
                    //嵌套collection
                    keySchema = getCollectionSchema(actualKeyType);
                } else if (Map.class.isAssignableFrom(keyClass)) {
                    //嵌套map
                    keySchema = getMapSchema(actualKeyType);
                } else {
                    //primitive or pojo
                }
            } else {
                keyClass = (Class) actualKeyType;
                if (keyClass.isArray()) {
                    //嵌套array
                    keySchema = getArraySchema(keyClass);
                } else if (Object.class.equals(keyClass)) {
                    // TODO: 2021/12/19 没有使用泛型, item类型为Object, 得动态处理
                    return null;
                } else {
                    //primitive or pojo
                }
            }

            Schema valueSchema = null;
            Class valueClass = null;
            if (actualValueType instanceof ParameterizedType) {
                ParameterizedType apt = (ParameterizedType) actualValueType;
                valueClass = (Class) apt.getRawType();

                if (Collection.class.isAssignableFrom(valueClass)) {
                    //嵌套collection
                    valueSchema = getCollectionSchema(actualValueType);
                } else if (Map.class.isAssignableFrom(valueClass)) {
                    //嵌套map
                    valueSchema = getMapSchema(actualValueType);
                } else {
                    //primitive or pojo
                }
            } else {
                valueClass = (Class) actualValueType;
                if (valueClass.isArray()) {
                    //嵌套array
                    valueSchema = getArraySchema(valueClass);
                } else if (Object.class.equals(valueClass)) {
                    // TODO: 2021/12/19 没有使用泛型, item类型为Object, 得动态处理
                    return null;
                } else {
                    //primitive or pojo
                }
            }

            return new MessageMapSchema(mapFactory, keyClass, keySchema, valueClass, valueSchema);
        } else {
            // TODO: 2021/12/19 没有使用泛型, item类型为Object, 得动态处理
            return null;
        }
    }

    /**
     * 获取指定类的消息id
     */
    @Nullable
    public static Integer getMessageId(Class claxx) {
        return idClassMap.inverse().get(claxx);
    }
}
