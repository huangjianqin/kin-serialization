package org.kin.kinbuffer.runtime;

import org.kin.framework.collection.CopyOnWriteMap;
import org.kin.framework.utils.ClassUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * bytes组成:
 * 写入byte, 标识是否是使用message id
 * = 0 : class name
 * = 1 : message id
 *
 * @author huangjianqin
 * @date 2021/12/24
 */
@SuppressWarnings({"rawtypes", "unchecked"})
final class ObjectSchema extends PolymorphicSchema<Object> {
    /** 单例 */
    static final ObjectSchema INSTANCE = new ObjectSchema();
    /** 缓存动态类型及其真实的{@link Class} */
    static final CopyOnWriteMap<String, Class> DYNAMIC_CLASS_CACHE = new CopyOnWriteMap<>();

    private ObjectSchema() {
    }

    @Override
    public Object read(Input input) {
        //读取类信息
        Class type;
        boolean useMessageId = input.readBoolean();
        String className = "";
        int messageId = 0;
        if (!useMessageId) {
            //class name
            className = input.readString();
            type = DYNAMIC_CLASS_CACHE.get(className);
            if (Objects.isNull(type)) {
                type = ClassUtils.getClass(className);
                DYNAMIC_CLASS_CACHE.put(className, type);
            }
        } else {
            //message id
            messageId = input.readVarInt32();
            //根据message id获取class
            type = Runtime.getClassByMessageId(messageId);
        }

        if (Objects.isNull(type)) {
            throw new IllegalArgumentException(String.format("doesn't find type, %s, %d", className, messageId));
        }

        //获取schema
        Schema schema = getSchema(type);
        if (Objects.isNull(schema)) {
            throw new IllegalArgumentException("can't not find schema for class ".concat(type.getName()));
        }

        //read
        return SchemaUtils.read(input, schema);
    }

    @Override
    public void write(Output output, Object o) {
        Class type = o.getClass();
        int messageId = Runtime.getMessageId(type);
        //写类信息
        if (messageId <=0) {
            //找不到message id, 写class name
            output.writeBoolean(false);
            output.writeString(type.getName());
        } else {
            //写message id
            output.writeBoolean(true);
            output.writeVarInt32(messageId);
        }

        //获取schema
        Schema schema = getSchema(type);
        if (Objects.isNull(schema)) {
            throw new IllegalArgumentException("can't not find schema for class ".concat(type.getName()));
        }

        //write
        schema.write(output, o);
    }

    /**
     * 根据实际类型不同获取对应处理的schema
     */
    private Schema getSchema(Class type) {
        if (Collection.class.isAssignableFrom(type)) {
            //实际类型是collection
            return MessageCollectionSchema.fromCache(type, Object.class, INSTANCE);
        } else if (Map.class.isAssignableFrom(type)) {
            //实际类型是map
            return MessageMapSchema.fromCache(type, Object.class, INSTANCE, Object.class, INSTANCE);
        } else if (type.isArray()) {
            //实际类型是array
            return new MessageArraySchema(type);
        } else if (Object.class.equals(type)) {
            //实际类型是object
            throw new IllegalArgumentException("dynamic type instance can't not be an java.lang.Object instance");
        } else {
            //实际类型是primitive or pojo
            return Runtime.getSchema(type);
        }
    }
}
