package org.kin.kinbuffer.runtime;

import org.kin.framework.utils.ExceptionUtils;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 类型组成:
 * 有符号变长int32(len):
 *      len > 0 : class name
 *      len < 0 : message id
 *
 * @author huangjianqin
 * @date 2021/12/24
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class ObjectSchema extends PolymorphicSchema {
    public static final ObjectSchema INSTANCE = new ObjectSchema();

    private ObjectSchema() {
    }

    @Override
    public Object read(Input input) {
        //读取类信息
        Class type = null;
        int len = input.readSInt32();
        String className = "";
        int messageId = 0;
        if (len > 0) {
            className = new String(input.readBytes(len), StandardCharsets.UTF_8);
            try {
                type = Class.forName(className);
            } catch (ClassNotFoundException e) {
                ExceptionUtils.throwExt(e);
            }
        } else {
            messageId = -len;
            type = Runtime.getClassByMessageId(messageId);
        }

        if (Objects.isNull(type)) {
            throw new IllegalArgumentException(String.format("doesn't find type, %s, %d", className, messageId));
        }

        Schema schema = getSchema(type);
        if (Objects.isNull(schema)) {
            throw new IllegalArgumentException("can't not find schema for class ".concat(type.getName()));
        }

        if (schema instanceof PolymorphicSchema) {
            return ((PolymorphicSchema) schema).read(input);
        } else {
            Object message = schema.newMessage();
            schema.merge(input, message);
            return message;
        }
    }

    @Override
    public void write(Output output, Object o) {
        Class type = o.getClass();
        Integer messageId = Runtime.getMessageId(type);
        //写类信息
        if (Objects.isNull(messageId)) {
            //找不到message id, 写class name
            String className = type.getName();
            output.writeSInt32(className.length());
            output.writeBytes(className.getBytes(StandardCharsets.UTF_8));
        } else {
            //写message id
            output.writeSInt32(-messageId);
        }

        Schema schema = getSchema(type);
        if (Objects.isNull(schema)) {
            throw new IllegalArgumentException("can't not find schema for class ".concat(type.getName()));
        }
        schema.write(output, o);
    }

    private Schema getSchema(Class type) {
        if (Collection.class.isAssignableFrom(type)) {
            //实际类型是collection
            return new MessageCollectionSchema<>(CollectionFactories.instance().getFactory(type), Object.class, INSTANCE);
        } else if (Map.class.isAssignableFrom(type)) {
            //实际类型是map
            return new MessageMapSchema<>(MapFactories.instance().getFactory(type), Object.class, INSTANCE, Object.class, INSTANCE);
        } else if (type.isArray()) {
            //实际类型是array
            return new MessageArraySchema<>(type.getComponentType(), getSchema(type.getComponentType()));
        } else if (Object.class.equals(type)) {
            //实际类型是object
            throw new IllegalArgumentException("dynamic type instance can't not be an java.lang.Object instance");
        } else {
            //实际类型是primitive or pojo
            return Runtime.getSchema(type);
        }
    }
}
