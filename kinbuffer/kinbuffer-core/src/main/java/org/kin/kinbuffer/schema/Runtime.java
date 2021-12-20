package org.kin.kinbuffer.schema;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import org.kin.framework.utils.ClassScanUtils;
import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.UnsafeUtil;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.schema.field.ByteBuddyField;
import org.kin.kinbuffer.schema.field.ReflectionField;
import org.kin.kinbuffer.schema.field.UnsafeField;

import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.*;

/**
 * @author huangjianqin
 * @date 2021/12/19
 */
@SuppressWarnings({"rawtypes", "unchecked", "ConstantConditions"})
public class Runtime {
    /** 使用支持字节码增强 */
    public static final boolean ENHANCE;

    static {
        Class<?> byteBuddyClass = null;
        try {
            byteBuddyClass = Class.forName("net.bytebuddy.ByteBuddy");
        } catch (Exception e) {
            //ignore
        }

        ENHANCE = Objects.nonNull(byteBuddyClass);
    }

    /** 基于copy-on-write更新, 以提高读性能 todo 是否可以以hashcode为key */
    private static volatile Map<String, Schema> schemas = new HashMap<>();
    /** todo 是否考虑动态扩容 */
    private static final BiMap<Integer, Class> idClassMap;
    private static final Method READ_METHOD;
    private static final Method WRITE_METHOD;

    static {
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

        try {
            READ_METHOD = Runtime.class.getMethod("read", Input.class, Class.class);
            WRITE_METHOD = Runtime.class.getMethod("write", Output.class, Object.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 获取{@code typeClass}的{@link Schema}实现
     */
    public static <T> Schema<T> getSchema(Class<T> typeClass) {
        Schema<T> schema = schemas.get(typeClass.getCanonicalName());
        if (Objects.isNull(schema)) {
            schema = constructSchema(typeClass);
        }
        return schema;
    }

    public static Object read(Input input, Class<?> typeClass) {
        if (String.class.equals(typeClass)) {
            return input.readString();
        } else if (Boolean.class.equals(typeClass) || Boolean.TYPE.equals(typeClass)) {
            return input.readBoolean();
        } else if (Byte.class.equals(typeClass) || Byte.TYPE.equals(typeClass)) {
            return (byte) input.readByte();
        } else if (Character.class.equals(typeClass) || Character.TYPE.equals(typeClass)) {
            return (char) input.readInt();
        } else if (Short.class.equals(typeClass) || Short.TYPE.equals(typeClass)) {
            return (short) input.readInt();
        } else if (Integer.class.equals(typeClass) || Integer.TYPE.equals(typeClass)) {
            return input.readInt();
        } else if (Long.class.equals(typeClass) || Long.TYPE.equals(typeClass)) {
            return input.readLong();
        } else if (Float.class.equals(typeClass) || Float.TYPE.equals(typeClass)) {
            return input.readFloat();
        } else if (Double.class.equals(typeClass) || Double.TYPE.equals(typeClass)) {
            return input.readDouble();
        } else {
            Schema schema = getSchema(typeClass);
            if (schema instanceof PolymorphicSchema) {
                return ((PolymorphicSchema) schema).read(input);
            } else {
                Object message = schema.newMessage();
                schema.merge(input, message);
                return message;
            }
        }
    }

    public static Object read(Input input, Class<?> typeClass, Schema schema) {
        if (Objects.isNull(schema)) {
            return read(input, typeClass);
        } else {
            if (schema instanceof ArraySchema) {
                return ((ArraySchema<?>) schema).read(input);
            } else {
                Object message = schema.newMessage();
                schema.merge(input, message);
                return message;
            }
        }
    }

    public static void write(Output output, Object target) {
        Class typeClass = target.getClass();
        if (String.class.equals(typeClass)) {
            output.writeString((String) target);
        } else if (Boolean.class.equals(typeClass) || Boolean.TYPE.equals(typeClass)) {
            output.writeBoolean((boolean) target);
        } else if (Byte.class.equals(typeClass) || Byte.TYPE.equals(typeClass)) {
            output.writeByte((byte) target);
        } else if (Character.class.equals(typeClass) || Character.TYPE.equals(typeClass)) {
            output.writeInt((char) target);
        } else if (Short.class.equals(typeClass) || Short.TYPE.equals(typeClass)) {
            output.writeInt((short) target);
        } else if (Integer.class.equals(typeClass) || Integer.TYPE.equals(typeClass)) {
            output.writeInt((int) target);
        } else if (Long.class.equals(typeClass) || Long.TYPE.equals(typeClass)) {
            output.writeLong((long) target);
        } else if (Float.class.equals(typeClass) || Float.TYPE.equals(typeClass)) {
            output.writeFloat((float) target);
        } else if (Double.class.equals(typeClass) || Double.TYPE.equals(typeClass)) {
            output.writeDouble((double) target);
        } else {
            getSchema(typeClass).write(output, target);
        }
    }

    public static void write(Output output, Object target, Schema schema) {
        if (Objects.isNull(schema)) {
            write(output, target);
        } else {
            schema.write(output, target);
        }
    }

    private static synchronized <T> Schema<T> constructSchema(Class<T> typeClass) {
        Schema<T> schema = constructSchema0(typeClass);

        Map<String, Schema> schemas = new HashMap<>(Runtime.schemas);
        schemas.put(typeClass.getCanonicalName(), schema);
        Runtime.schemas = schemas;

        return schema;
    }

    private static <T> Schema<T> constructSchema0(Class<T> typeClass) {
        if(typeClass.isEnum()){
            return (Schema<T>)new EnumSchema<>((Class<? extends Enum>)typeClass);
        }
        else{
            return constructPoJoSchema0(typeClass);
        }
    }

    private static <T> Schema<T> constructPoJoSchema0(Class<T> typeClass) {
        List<Field> allFields = ClassUtils.getAllFields(typeClass);
        List<org.kin.kinbuffer.schema.field.Field> fields = new ArrayList<>(allFields.size());
        for (java.lang.reflect.Field field : allFields) {
            int modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers) ||
                    Modifier.isStatic(modifiers) ||
                    Modifier.isTransient(modifiers)) {
                continue;
            }

            Class<?> type = field.getType();
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
            //嵌套
            return new ArraySchema(getCollectionSchema(componentType), componentType);
        } else if (Map.class.isAssignableFrom(componentType)) {
            //嵌套
            return new ArraySchema(getMapSchema(componentType), componentType);
        } else if (componentType.isArray()) {
            //嵌套
            return new ArraySchema(getArraySchema(componentType), componentType);
        } else if (Object.class.equals(componentType)) {
            // TODO: 2021/12/19 没有使用泛型, item类型为Object, 得动态处理
            return null;
        } else {
            return new ArraySchema(componentType);
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
                    //嵌套
                    return new CollectionSchema(getCollectionSchema(actualType), collectionFactory);
                } else if (Map.class.isAssignableFrom(ac)) {
                    //嵌套
                    return new CollectionSchema(getMapSchema(actualType), collectionFactory);
                } else if (ac.isArray()) {
                    //嵌套
                    return new CollectionSchema(getArraySchema((Class) actualType), collectionFactory);
                }
            }

            //primitive or pojo
            return new CollectionSchema<>(collectionFactory, (Class) actualType);
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
                Class akc = (Class) apt.getRawType();

                if (Collection.class.isAssignableFrom(akc)) {
                    //嵌套
                    keySchema = getCollectionSchema(actualKeyType);
                } else if (Map.class.isAssignableFrom(akc)) {
                    //嵌套
                    keySchema = getMapSchema(actualKeyType);
                } else if (akc.isArray()) {
                    //嵌套
                    keySchema = getArraySchema((Class) actualKeyType);
                }
            } else {
                keyClass = (Class) actualKeyType;
            }

            Schema valueSchema = null;
            Class valueClass = null;
            if (actualValueType instanceof ParameterizedType) {
                ParameterizedType apt = (ParameterizedType) actualValueType;
                Class akc = (Class) apt.getRawType();

                if (Collection.class.isAssignableFrom(akc)) {
                    //嵌套
                    valueSchema = getCollectionSchema(actualValueType);
                } else if (Map.class.isAssignableFrom(akc)) {
                    //嵌套
                    valueSchema = getMapSchema(actualValueType);
                } else if (akc.isArray()) {
                    //嵌套
                    valueSchema = getArraySchema((Class) actualValueType);
                }
            } else {
                valueClass = (Class) actualValueType;
            }

            //primitive or pojo
            return new MapSchema(mapFactory, keyClass, keySchema, valueClass, valueSchema);
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
