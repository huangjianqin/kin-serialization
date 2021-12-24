package org.kin.kinbuffer.runtime;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import org.kin.framework.collection.Tuple;
import org.kin.framework.utils.ClassScanUtils;
import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.UnsafeUtil;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.field.ByteBuddyField;
import org.kin.kinbuffer.runtime.field.ReflectionField;
import org.kin.kinbuffer.runtime.field.UnsafeField;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author huangjianqin
 * @date 2021/12/19
 */
@SuppressWarnings({"rawtypes", "unchecked", "ConstantConditions"})
public final class Runtime {
    private Runtime() {
    }

    /** 内部保留的消息id数量 */
    private static final int RETAIN_MESSAGE_ID_NUM = 200;
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
            int id = messageId.id() + RETAIN_MESSAGE_ID_NUM;
            if (id <= RETAIN_MESSAGE_ID_NUM) {
                throw new IllegalStateException(String.format("message id must be int range (0, 2147483447], id '%d', class '%s'", id, claxx.getCanonicalName()));
            }
            if (!messageIds.add(id)) {
                throw new IllegalStateException(String.format("duplication message id '%d', class '%s'", id, claxx.getCanonicalName()));
            }
            idClassMapBuilder.put(id, claxx);
        }

        //框架内置message id
        //primitive
        idClassMapBuilder.put(1, String.class);
        idClassMapBuilder.put(2, Boolean.class);
        idClassMapBuilder.put(3, Boolean.TYPE);
        idClassMapBuilder.put(4, Byte.class);
        idClassMapBuilder.put(5, Byte.TYPE);
        idClassMapBuilder.put(6, Character.class);
        idClassMapBuilder.put(7, Character.TYPE);
        idClassMapBuilder.put(8, Short.class);
        idClassMapBuilder.put(9, Short.TYPE);
        idClassMapBuilder.put(10, Integer.class);
        idClassMapBuilder.put(11, Integer.TYPE);
        idClassMapBuilder.put(12, Long.class);
        idClassMapBuilder.put(13, Long.TYPE);
        idClassMapBuilder.put(14, Float.class);
        idClassMapBuilder.put(15, Float.TYPE);
        idClassMapBuilder.put(16, Double.class);
        idClassMapBuilder.put(17, Double.TYPE);
        //->30, 留着扩展

        //collection
        idClassMapBuilder.put(31, ArrayList.class);
        idClassMapBuilder.put(32, LinkedList.class);
        idClassMapBuilder.put(33, CopyOnWriteArrayList.class);
        idClassMapBuilder.put(34, Stack.class);
        idClassMapBuilder.put(35, Vector.class);
        idClassMapBuilder.put(36, HashSet.class);
        idClassMapBuilder.put(37, LinkedHashSet.class);
        idClassMapBuilder.put(38, TreeSet.class);
        idClassMapBuilder.put(39, ConcurrentSkipListSet.class);
        idClassMapBuilder.put(40, CopyOnWriteArraySet.class);
        idClassMapBuilder.put(41, LinkedBlockingQueue.class);
        idClassMapBuilder.put(42, LinkedBlockingDeque.class);
        idClassMapBuilder.put(43, ArrayBlockingQueue.class);
        idClassMapBuilder.put(44, ArrayDeque.class);
        idClassMapBuilder.put(45, ConcurrentLinkedQueue.class);
        idClassMapBuilder.put(46, ConcurrentLinkedDeque.class);
        idClassMapBuilder.put(47, PriorityBlockingQueue.class);
        idClassMapBuilder.put(48, PriorityQueue.class);
        //->70, 留着扩展

        //map
        idClassMapBuilder.put(71, HashMap.class);
        idClassMapBuilder.put(72, TreeMap.class);
        idClassMapBuilder.put(73, LinkedHashMap.class);
        idClassMapBuilder.put(74, WeakHashMap.class);
        idClassMapBuilder.put(75, IdentityHashMap.class);
        idClassMapBuilder.put(76, Hashtable.class);
        idClassMapBuilder.put(77, ConcurrentHashMap.class);
        idClassMapBuilder.put(78, ConcurrentSkipListMap.class);
        idClassMapBuilder.put(79, Properties.class);
        //->100, 留着扩展

        idClassMap = idClassMapBuilder.build();
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

    /**
     * 获取{@code typeClass}的{@link Schema}实现
     */
    private static synchronized <T> Schema<T> constructSchema(Class<T> typeClass) {
        Schema<T> schema = constructSchema0(typeClass);

        Map<String, Schema> schemas = new HashMap<>(Runtime.schemas);
        schemas.put(typeClass.getName(), schema);
        Runtime.schemas = schemas;

        return schema;
    }

    /**
     * 获取{@code typeClass}的{@link Schema}实现
     */
    private static <T> Schema<T> constructSchema0(Class<T> typeClass) {
        if (typeClass.isEnum()) {
            return (Schema<T>) new EnumSchema<>((Class<? extends Enum>) typeClass);
        } else {
            return constructPoJoSchema0(typeClass);
        }
    }

    /**
     * 获取{@code typeClass}的{@link Schema}实现
     */
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
            } else if (Object.class.equals(type)) {
                schema = ObjectSchema.INSTANCE;
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

    /**
     * 解析返回{@link java.lang.reflect.Array}或{@link Collection}的item运行时类型和{@link Map}key和value运行时类型, 返回对应的{@link Schema}实现
     *
     * @param type item类型
     */
    @Nonnull
    private static Tuple<Class, Schema> getItemClassSchema(Type type) {
        Class ac;
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            ac = (Class) pt.getRawType();

            if (Collection.class.isAssignableFrom(ac)) {
                return new Tuple<>(ac, getCollectionSchema(type));
            } else if (Map.class.isAssignableFrom(ac)) {
                return new Tuple<>(ac, getMapSchema(type));
            } else {
                //primitive or pojo
            }
        } else {
            ac = (Class) type;
        }

        if (Collection.class.isAssignableFrom(ac)) {
            return new Tuple<>(ac, ObjectSchema.INSTANCE);
        } else if (Map.class.isAssignableFrom(ac)) {
            return new Tuple<>(ac, ObjectSchema.INSTANCE);
        } else if (ac.isArray()) {
            return new Tuple<>(ac, getArraySchema(ac));
        } else if (Object.class.equals(ac)) {
            return new Tuple<>(ac, ObjectSchema.INSTANCE);
        } else {
            //primitive or pojo
            return new Tuple<>(ac, null);
        }
    }

    /**
     * 解析{@link java.lang.reflect.Array}item类型并返回该array的{@link Schema}实现
     *
     * @param type field字段类型{@link Field#getType()} or 嵌套的item类型, 比如{@code int[][]}
     */
    private static Schema getArraySchema(Class type) {
        Tuple<Class, Schema> classSchema = getItemClassSchema(type.getComponentType());
        return new MessageArraySchema(classSchema.first(), classSchema.second());
    }

    /**
     * 解析{@link Collection}item类型并返回该collection的{@link Schema}实现
     *
     * @param type field字段类型{@link Field#getGenericType()} or 嵌套的item类型, 比如{@code List<List<?>>}
     */
    private static Schema getCollectionSchema(Type type) {
        CollectionFactory<?> collectionFactory;
        Type itemType;
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            itemType = pt.getActualTypeArguments()[0];
            collectionFactory = CollectionFactories.getFactory((Class<?>) pt.getRawType());
        } else {
            itemType = Object.class;
            collectionFactory = CollectionFactories.getFactory(type.getClass());
        }

        Tuple<Class, Schema> classSchema = getItemClassSchema(itemType);
        return new MessageCollectionSchema(collectionFactory, classSchema.first(), classSchema.second());
    }

    /**
     * 解析{@link Map}key和value类型并返回该map的{@link Schema}实现
     *
     * @param type field字段类型, {@link Field#getGenericType()} or 嵌套的item类型, 比如{@code Map<Integer, Map<?,?>>}
     */
    private static Schema getMapSchema(Type type) {
        MapFactory mapFactory;
        Type keyType;
        Type valueType;
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            keyType = pt.getActualTypeArguments()[0];
            valueType = pt.getActualTypeArguments()[1];
            mapFactory = MapFactory.getFactory((Class<? extends Map<?, ?>>) pt.getRawType());
        } else {
            keyType = Object.class;
            valueType = Object.class;
            mapFactory = MapFactory.getFactory((Class<? extends Map<?, ?>>) type);
        }

        Tuple<Class, Schema> keyClassSchema = getItemClassSchema(keyType);
        Tuple<Class, Schema> valueClassSchema = getItemClassSchema(valueType);
        return new MessageMapSchema(mapFactory, keyClassSchema.first(), keyClassSchema.second(), valueClassSchema.first(), valueClassSchema.second());
    }

    /**
     * 获取指定类的消息id
     */
    @Nullable
    public static Integer getMessageId(Class claxx) {
        return idClassMap.inverse().get(claxx);
    }

    /**
     * 获取消息id获取消息类
     */
    @Nullable
    public static Class getClassByMessageId(int messageId) {
        return idClassMap.get(messageId);
    }
}
