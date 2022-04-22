package org.kin.kinbuffer.runtime;

import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.eclipse.collections.impl.factory.primitive.ObjectIntMaps;
import org.kin.framework.collection.Tuple;
import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.CollectionUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.runtime.field.*;

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
@SuppressWarnings({"rawtypes", "unchecked"})
public final class Runtime {
    private Runtime() {
    }

    /** 反射 */
    private static final byte REFLECTION_TYPE = 1;
    /** unsafe */
    private static final byte UNSAFE_TYPE = 2;
    /** 基于jdk自带的{@link java.lang.invoke.LambdaMetafactory}生成setter和getter方法代理 */
    private static final byte ENHANCE_TYPE = 3;

    /** 基于copy-on-write更新, 以提高读性能 todo 是否可以以hashcode为key */
    private static volatile Map<String, Schema> schemas = new HashMap<>();
    /** key -> message id, value -> message class */
    private static volatile MutableIntObjectMap<Class> ID_CLASS_MAP;
    /** key -> message class, value -> message id */
    private static volatile MutableObjectIntMap<Class> CLASS_ID_MAP;
    /** 决定使用哪个{@link org.kin.kinbuffer.runtime.field.Field}实现类 */
    private static volatile byte fieldType = 0;

    static {
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

        //框架内置message id
        MutableIntObjectMap<Class> idClassMap = IntObjectMaps.mutable.withInitialCapacity(128);
        MutableObjectIntMap<Class> classIdMap = ObjectIntMaps.mutable.withInitialCapacity(128);
        //primitive
        int i = 1;
        idClassMap.put(i++, String.class);
        idClassMap.put(i++, Boolean.class);
        idClassMap.put(i++, Boolean.TYPE);
        idClassMap.put(i++, Byte.class);
        idClassMap.put(i++, Byte.TYPE);
        idClassMap.put(i++, Character.class);
        idClassMap.put(i++, Character.TYPE);
        idClassMap.put(i++, Short.class);
        idClassMap.put(i++, Short.TYPE);
        idClassMap.put(i++, Integer.class);
        idClassMap.put(i++, Integer.TYPE);
        idClassMap.put(i++, Long.class);
        idClassMap.put(i++, Long.TYPE);
        idClassMap.put(i++, Float.class);
        idClassMap.put(i++, Float.TYPE);
        idClassMap.put(i++, Double.class);
        idClassMap.put(i, Double.TYPE);

        i = 1;
        classIdMap.put(String.class, i++);
        classIdMap.put(Boolean.class, i++);
        classIdMap.put(Boolean.TYPE, i++);
        classIdMap.put(Byte.class, i++);
        classIdMap.put(Byte.TYPE, i++);
        classIdMap.put(Character.class, i++);
        classIdMap.put(Character.TYPE, i++);
        classIdMap.put(Short.class, i++);
        classIdMap.put(Short.TYPE, i++);
        classIdMap.put(Integer.class, i++);
        classIdMap.put(Integer.TYPE, i++);
        classIdMap.put(Long.class, i++);
        classIdMap.put(Long.TYPE, i++);
        classIdMap.put(Float.class, i++);
        classIdMap.put(Float.TYPE, i++);
        classIdMap.put(Double.class, i++);
        classIdMap.put(Double.TYPE, i);
        //->30, 留着扩展

        //collection
        i = 31;
        idClassMap.put(i++, ArrayList.class);
        idClassMap.put(i++, LinkedList.class);
        idClassMap.put(i++, CopyOnWriteArrayList.class);
        idClassMap.put(i++, Stack.class);
        idClassMap.put(i++, Vector.class);
        idClassMap.put(i++, HashSet.class);
        idClassMap.put(i++, LinkedHashSet.class);
        idClassMap.put(i++, TreeSet.class);
        idClassMap.put(i++, ConcurrentSkipListSet.class);
        idClassMap.put(i++, CopyOnWriteArraySet.class);
        idClassMap.put(i++, LinkedBlockingQueue.class);
        idClassMap.put(i++, LinkedBlockingDeque.class);
        idClassMap.put(i++, ArrayBlockingQueue.class);
        idClassMap.put(i++, ArrayDeque.class);
        idClassMap.put(i++, ConcurrentLinkedQueue.class);
        idClassMap.put(i++, ConcurrentLinkedDeque.class);
        idClassMap.put(i++, PriorityBlockingQueue.class);
        idClassMap.put(i++, PriorityQueue.class);
        //注册Singleton或Empty Collection
        idClassMap.put(i++, ClassUtils.getClass("java.util.Arrays$ArrayList"));
        idClassMap.put(i++, ClassUtils.getClass("java.util.Collections$SingletonList"));
        idClassMap.put(i++, ClassUtils.getClass("java.util.Collections$SingletonSet"));
        idClassMap.put(i++, ClassUtils.getClass("java.util.Collections$EmptyList"));
        idClassMap.put(i++, ClassUtils.getClass("java.util.Collections$EmptySet"));
        idClassMap.put(i++, ClassUtils.getClass("java.util.Collections$UnmodifiableNavigableSet$EmptyNavigableSet"));
        idClassMap.put(i++, ClassUtils.getClass("java.util.Collections$UnmodifiableCollection"));
        idClassMap.put(i++, ClassUtils.getClass("java.util.Collections$UnmodifiableList"));
        idClassMap.put(i++, ClassUtils.getClass("java.util.Collections$UnmodifiableSet"));
        idClassMap.put(i++, ClassUtils.getClass("java.util.Collections$UnmodifiableNavigableSet"));
        //注册基础类型二维数组
        idClassMap.put(i++, byte[].class);
        idClassMap.put(i++, byte[][].class);
        idClassMap.put(i++, char[].class);
        idClassMap.put(i++, char[][].class);
        idClassMap.put(i++, short[].class);
        idClassMap.put(i++, short[][].class);
        idClassMap.put(i++, int[].class);
        idClassMap.put(i++, int[][].class);
        idClassMap.put(i++, float[].class);
        idClassMap.put(i++, float[][].class);
        idClassMap.put(i++, long[].class);
        idClassMap.put(i++, long[][].class);
        idClassMap.put(i++, double[].class);
        idClassMap.put(i++, double[][].class);
        idClassMap.put(i++, Byte[].class);
        idClassMap.put(i++, Byte[][].class);
        idClassMap.put(i++, Character[].class);
        idClassMap.put(i++, Character[][].class);
        idClassMap.put(i++, Short[].class);
        idClassMap.put(i++, Short[][].class);
        idClassMap.put(i++, Integer[].class);
        idClassMap.put(i++, Integer[][].class);
        idClassMap.put(i++, Float[].class);
        idClassMap.put(i++, Float[][].class);
        idClassMap.put(i++, Long[].class);
        idClassMap.put(i++, Long[][].class);
        idClassMap.put(i++, Double[].class);
        idClassMap.put(i, Double[][].class);

        i = 31;
        classIdMap.put(ArrayList.class, i++);
        classIdMap.put(LinkedList.class, i++);
        classIdMap.put(CopyOnWriteArrayList.class, i++);
        classIdMap.put(Stack.class, i++);
        classIdMap.put(Vector.class, i++);
        classIdMap.put(HashSet.class, i++);
        classIdMap.put(LinkedHashSet.class, i++);
        classIdMap.put(TreeSet.class, i++);
        classIdMap.put(ConcurrentSkipListSet.class, i++);
        classIdMap.put(CopyOnWriteArraySet.class, i++);
        classIdMap.put(LinkedBlockingQueue.class, i++);
        classIdMap.put(LinkedBlockingDeque.class, i++);
        classIdMap.put(ArrayBlockingQueue.class, i++);
        classIdMap.put(ArrayDeque.class, i++);
        classIdMap.put(ConcurrentLinkedQueue.class, i++);
        classIdMap.put(ConcurrentLinkedDeque.class, i++);
        classIdMap.put(PriorityBlockingQueue.class, i++);
        classIdMap.put(PriorityQueue.class, i++);
        //注册Singleton或Empty Collection
        classIdMap.put(ClassUtils.getClass("java.util.Arrays$ArrayList"), i++);
        classIdMap.put(ClassUtils.getClass("java.util.Collections$SingletonList"), i++);
        classIdMap.put(ClassUtils.getClass("java.util.Collections$SingletonSet"), i++);
        classIdMap.put(ClassUtils.getClass("java.util.Collections$EmptyList"), i++);
        classIdMap.put(ClassUtils.getClass("java.util.Collections$EmptySet"), i++);
        classIdMap.put(ClassUtils.getClass("java.util.Collections$UnmodifiableNavigableSet$EmptyNavigableSet"), i++);
        classIdMap.put(ClassUtils.getClass("java.util.Collections$UnmodifiableCollection"), i++);
        classIdMap.put(ClassUtils.getClass("java.util.Collections$UnmodifiableList"), i++);
        classIdMap.put(ClassUtils.getClass("java.util.Collections$UnmodifiableSet"), i++);
        classIdMap.put(ClassUtils.getClass("java.util.Collections$UnmodifiableNavigableSet"), i++);
        //注册基础类型二维数组
        classIdMap.put(byte[].class, i++);
        classIdMap.put(byte[][].class, i++);
        classIdMap.put(char[].class, i++);
        classIdMap.put(char[][].class, i++);
        classIdMap.put(short[].class, i++);
        classIdMap.put(short[][].class, i++);
        classIdMap.put(int[].class, i++);
        classIdMap.put(int[][].class, i++);
        classIdMap.put(float[].class, i++);
        classIdMap.put(float[][].class, i++);
        classIdMap.put(long[].class, i++);
        classIdMap.put(long[][].class, i++);
        classIdMap.put(double[].class, i++);
        classIdMap.put(double[][].class, i++);
        classIdMap.put(Byte[].class, i++);
        classIdMap.put(Byte[][].class, i++);
        classIdMap.put(Character[].class, i++);
        classIdMap.put(Character[][].class, i++);
        classIdMap.put(Short[].class, i++);
        classIdMap.put(Short[][].class, i++);
        classIdMap.put(Integer[].class, i++);
        classIdMap.put(Integer[][].class, i++);
        classIdMap.put(Float[].class, i++);
        classIdMap.put(Float[][].class, i++);
        classIdMap.put(Long[].class, i++);
        classIdMap.put(Long[][].class, i++);
        classIdMap.put(Double[].class, i++);
        classIdMap.put(Double[][].class, i);
        //->70, 留着扩展

        //map
        i = 71;
        idClassMap.put(i++, HashMap.class);
        idClassMap.put(i++, TreeMap.class);
        idClassMap.put(i++, LinkedHashMap.class);
        idClassMap.put(i++, WeakHashMap.class);
        idClassMap.put(i++, IdentityHashMap.class);
        idClassMap.put(i++, Hashtable.class);
        idClassMap.put(i++, ConcurrentHashMap.class);
        idClassMap.put(i++, ConcurrentSkipListMap.class);
        idClassMap.put(i++, Properties.class);
        idClassMap.put(i++, ClassUtils.getClass("java.util.Collections$SingletonMap"));
        idClassMap.put(i++, ClassUtils.getClass("java.util.Collections$EmptyMap"));
        idClassMap.put(i++, ClassUtils.getClass("java.util.Collections$UnmodifiableNavigableMap$EmptyNavigableMap"));
        idClassMap.put(i++, ClassUtils.getClass("java.util.Collections$UnmodifiableNavigableMap"));
        idClassMap.put(i, ClassUtils.getClass("java.util.Collections$UnmodifiableMap"));

        i = 71;
        classIdMap.put(HashMap.class, i++);
        classIdMap.put(TreeMap.class, i++);
        classIdMap.put(LinkedHashMap.class, i++);
        classIdMap.put(WeakHashMap.class, i++);
        classIdMap.put(IdentityHashMap.class, i++);
        classIdMap.put(Hashtable.class, i++);
        classIdMap.put(ConcurrentHashMap.class, i++);
        classIdMap.put(ConcurrentSkipListMap.class, i++);
        classIdMap.put(Properties.class, i++);
        classIdMap.put(ClassUtils.getClass("java.util.Collections$SingletonMap"), i++);
        classIdMap.put(ClassUtils.getClass("java.util.Collections$EmptyMap"), i++);
        classIdMap.put(ClassUtils.getClass("java.util.Collections$UnmodifiableNavigableMap$EmptyNavigableMap"), i++);
        classIdMap.put(ClassUtils.getClass("java.util.Collections$UnmodifiableNavigableMap"), i++);
        classIdMap.put(ClassUtils.getClass("java.util.Collections$UnmodifiableMap"), i);
        //->100, 留着扩展

        //-200, 内部保留使用
        ID_CLASS_MAP = IntObjectMaps.mutable.ofAll(idClassMap);
        CLASS_ID_MAP = ObjectIntMaps.mutable.ofAll(classIdMap);
    }

    /**
     * 给定{@link Schema}, 从{@code input} 中读取并反序列化pojo
     */
    public static Object read(Input input, Schema schema) {
        if (schema instanceof PolymorphicSchema) {
            return ((PolymorphicSchema) schema).read(input);
        } else {
            Object message = schema.newMessage();
            schema.merge(input, message);
            return message;
        }
    }

    /**
     * 给定{@link Schema}, 将{@code target}序列化并输出到{@code output}
     */
    public static void write(Output output, Object target, Schema schema) {
        if (Objects.isNull(schema)) {
            Class typeClass = target.getClass();
            getSchema(typeClass).write(output, target);
        } else {
            schema.write(output, target);
        }
    }

    /**
     * 使用{@link org.kin.kinbuffer.runtime.field.ReflectionField}
     */
    public static void useReflection() {
        useFieldType(REFLECTION_TYPE);
    }

    /**
     * 使用{@link org.kin.kinbuffer.runtime.field.UnsafeField}
     */
    public static void useUnsafe() {
        useFieldType(UNSAFE_TYPE);
    }

    /**
     * 使用{@link org.kin.kinbuffer.runtime.field.EnhanceField}
     */
    public static void useEnhance() {
        useFieldType(ENHANCE_TYPE);
    }

    /**
     * 决定使用哪个{@link org.kin.kinbuffer.runtime.field.Field}实现类
     */
    private static synchronized void useFieldType(byte type) {
        if (type != REFLECTION_TYPE &&
                type != UNSAFE_TYPE &&
                type != ENHANCE_TYPE) {
            throw new IllegalStateException("field type value is illegal");
        }

        if (fieldType != 0) {
            throw new IllegalStateException("field type just can set one times");
        }

        fieldType = type;
    }

    /**
     * 注册message id及其message class
     */
    public static <T> void registerSchema(Class<T> typeClass, Schema<T> schema) {
        registerSchema0(typeClass, schema);
        registerMessageIdClass(typeClass);
    }

    /**
     * 注册message class及自定义{@link Schema}实现
     */
    public static synchronized <T> void registerSchema0(Class<T> typeClass, Schema<T> schema) {
        String className = typeClass.getName();
        if (schemas.containsKey(className)) {
            throw new IllegalArgumentException(String.format("type '%s' has registered schema", className));
        }

        Map<String, Schema> schemas = new HashMap<>(Runtime.schemas);
        schemas.put(className, schema);
        Runtime.schemas = schemas;
    }

    /**
     * 获取{@code typeClass}的{@link Schema}实现
     */
    public static <T> Schema<T> getSchema(Class<T> typeClass) {
        Schema<T> schema = schemas.get(typeClass.getName());
        if (Objects.isNull(schema)) {
            schema = constructSchema(typeClass);

            registerMessageIdClass(typeClass);
        }
        return schema;
    }

    /**
     * 获取{@code typeClass}的{@link Schema}实现
     */
    private static synchronized <T> Schema<T> constructSchema(Class<T> typeClass) {
        Schema<T> schema = schemas.get(typeClass.getName());
        if (Objects.nonNull(schema)) {
            return schema;
        }

        if (fieldType == 0) {
            //默认使用jdk自带的lambda字节码增加
            useEnhance();
        }

        schema = constructSchema0(typeClass);
        registerSchema0(typeClass, schema);
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

            if (type.isPrimitive()) {
                //primitive
                org.kin.kinbuffer.runtime.field.Field primitiveField = tryConstructPrimitiveField(field);
                if (Objects.nonNull(primitiveField)) {
                    fields.add(primitiveField);
                    continue;
                }
                //rollback to primitive object
            }

            Schema schema;
            if (type.isPrimitive()) {
                //primitive
                schema = schemas.get(type.getName());
            } else if (Collection.class.isAssignableFrom(type)) {
                schema = getCollectionSchema(field.getGenericType());
            } else if (Map.class.isAssignableFrom(type)) {
                schema = getMapSchema(field.getGenericType());
            } else if (type.isArray()) {
                schema = getArraySchema(type);
            } else if (Object.class.equals(type) || Modifier.isAbstract(type.getModifiers())) {
                schema = ObjectSchema.INSTANCE;
            } else {
                //pojo
                schema = schemas.get(type.getName());
            }

            fields.add(constructField(field, schema));
        }

        return new RuntimeSchema<>(typeClass, fields);
    }

    /**
     * 尝试创建{@link org.kin.kinbuffer.runtime.field.PrimitiveUnsafeField}, 如果使用unsafe, 可以减少包装类的装包和解包
     */
    @Nullable
    private static org.kin.kinbuffer.runtime.field.Field tryConstructPrimitiveField(Field field) {
        Class<?> type = field.getType();
        if (Boolean.TYPE.equals(type)) {
            return new BooleanUnsafeField(field);
        } else if (Byte.TYPE.equals(type)) {
            return new ByteUnsafeField(field);
        } else if (Character.TYPE.equals(type)) {
            return new CharUnsafeField(field);
        } else if (Short.TYPE.equals(type)) {
            return new ShortUnsafeField(field);
        } else if (Integer.TYPE.equals(type)) {
            return new IntUnsafeField(field);
        } else if (Long.TYPE.equals(type)) {
            return new LongUnsafeField(field);
        } else if (Float.TYPE.equals(type)) {
            return new FloatUnsafeField(field);
        } else if (Double.TYPE.equals(type)) {
            return new DoubleUnsafeField(field);
        } else {
            return null;
        }
    }

    /**
     * 根据使用者选择的{@link #fieldType}来创建{@link org.kin.kinbuffer.runtime.field.Field}实例
     */
    private static org.kin.kinbuffer.runtime.field.Field constructField(Field field, Schema schema) {
        switch (fieldType) {
            case REFLECTION_TYPE:
                return new ReflectionField(field, schema);
            case UNSAFE_TYPE:
                return new UnsafeField(field, schema);
            case ENHANCE_TYPE:
                return new EnhanceField(field, schema);
            default:
                throw new IllegalStateException("field type is not set");
        }
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
                return new Tuple<>(ac, schemas.get(ac.getName()));
            }
        } else {
            ac = (Class) type;
        }

        if (ac.isPrimitive()) {
            //primitive
            return new Tuple<>(ac, schemas.get(ac.getName()));
        } else if (Collection.class.isAssignableFrom(ac)) {
            return new Tuple<>(ac, ObjectSchema.INSTANCE);
        } else if (Map.class.isAssignableFrom(ac)) {
            return new Tuple<>(ac, ObjectSchema.INSTANCE);
        } else if (ac.isArray()) {
            return new Tuple<>(ac, getArraySchema(ac));
        } else if (Object.class.equals(ac) || Modifier.isAbstract(ac.getModifiers())) {
            return new Tuple<>(ac, ObjectSchema.INSTANCE);
        } else {
            //primitive or pojo
            return new Tuple<>(ac, schemas.get(ac.getName()));
        }
    }

    /**
     * 解析{@link java.lang.reflect.Array}item类型并返回该array的{@link Schema}实现
     *
     * @param type field字段类型{@link Field#getType()} or 嵌套的item类型, 比如{@code int[][]}
     */
    private static Schema getArraySchema(Class type) {
        return new MessageArraySchema(type);
    }

    /**
     * 解析{@link Collection}item类型并返回该collection的{@link Schema}实现
     *
     * @param type field字段类型{@link Field#getGenericType()} or 嵌套的item类型, 比如{@code List<List<?>>}
     */
    private static Schema getCollectionSchema(Type type) {
        Class<?> collectionType;
        Type itemType;
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            collectionType = (Class<?>) pt.getRawType();
            itemType = pt.getActualTypeArguments()[0];
        } else {
            collectionType = type.getClass();
            itemType = Object.class;
        }

        Tuple<Class, Schema> classSchema = getItemClassSchema(itemType);
        return new MessageCollectionSchema(collectionType, classSchema.first(), classSchema.second());
    }

    /**
     * 解析{@link Map}key和value类型并返回该map的{@link Schema}实现
     *
     * @param type field字段类型, {@link Field#getGenericType()} or 嵌套的item类型, 比如{@code Map<Integer, Map<?,?>>}
     */
    private static Schema getMapSchema(Type type) {
        Class<?> mapType;
        Type keyType;
        Type valueType;
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            mapType = (Class<? extends Map<?, ?>>) pt.getRawType();
            keyType = pt.getActualTypeArguments()[0];
            valueType = pt.getActualTypeArguments()[1];
        } else {
            mapType = (Class<? extends Map<?, ?>>) type;
            keyType = Object.class;
            valueType = Object.class;
        }

        Tuple<Class, Schema> keyClassSchema = getItemClassSchema(keyType);
        Tuple<Class, Schema> valueClassSchema = getItemClassSchema(valueType);
        return new MessageMapSchema(mapType, keyClassSchema.first(), keyClassSchema.second(), valueClassSchema.first(), valueClassSchema.second());
    }

    /**
     * 获取指定类的消息id
     */
    static int getMessageId(Class clazz) {
        return CLASS_ID_MAP.get(clazz);
    }

    /**
     * 根据消息id获取消息类
     */
    static Class getClassByMessageId(int messageId) {
        return ID_CLASS_MAP.get(messageId);
    }

    /**
     * 注册message id及其message class
     */
    public static synchronized void registerMessageIdClass(int messageId, Class<?> clazz) {
        if (messageId <= 0) {
            throw new IllegalArgumentException("messageId must be greater than 0");
        }

        if (ID_CLASS_MAP.containsKey(messageId) || CLASS_ID_MAP.containsKey(clazz) ) {
            throw new IllegalArgumentException(String.format("message class '%s' or message id `%d` has registered", clazz.getName(), messageId));
        }

        if (Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalArgumentException(String.format("message class '%s' is abstract", clazz.getName()));
        }

        registerMessageIdClass0(Collections.singletonList(new Tuple<>(messageId, clazz)));
    }

    /**
     * 注册message id及其message class
     */
    public static synchronized void registerMessageIdClass(Class<?> clazz) {
        List<Tuple<Integer, Class<?>>> messageIdClassTuples = new ArrayList<>(4);
        for (Class<?> inheritanceClass : ClassUtils.getAllClasses(clazz)) {
            if (Object.class.equals(inheritanceClass) || Modifier.isAbstract(inheritanceClass.getModifiers())) {
                continue;
            }

            MessageId messageIdAnno = inheritanceClass.getAnnotation(MessageId.class);
            if (Objects.isNull(messageIdAnno)) {
                continue;
            }

            int messageId = messageIdAnno.value();
            if (messageId <= 0) {
                throw new IllegalArgumentException("messageId must be greater than 0");
            }

            if (ID_CLASS_MAP.containsKey(messageId) || CLASS_ID_MAP.containsKey(clazz) ) {
                continue;
            }

            messageIdClassTuples.add(new Tuple<>(messageId, inheritanceClass));
        }

        registerMessageIdClass0(messageIdClassTuples);
    }

    /**
     * 注册message id及其message class
     */
    public static synchronized void registerMessageIdClass0(List<Tuple<Integer, Class<?>>> messageIdClassTuples) {
        if (CollectionUtils.isEmpty(messageIdClassTuples)) {
            return;
        }

        MutableIntObjectMap<Class> idClassMap = IntObjectMaps.mutable.ofAll(Runtime.ID_CLASS_MAP);
        MutableObjectIntMap<Class> classIdMap = ObjectIntMaps.mutable.ofAll(Runtime.CLASS_ID_MAP);

        for (Tuple<Integer, Class<?>> tuple : messageIdClassTuples) {
            Integer messageId = tuple.first();
            Class<?> clazz = tuple.second();

            idClassMap.put(messageId, clazz);
            classIdMap.put(clazz, messageId);
        }

        Runtime.ID_CLASS_MAP = idClassMap;
        Runtime.CLASS_ID_MAP = classIdMap;
    }
}
