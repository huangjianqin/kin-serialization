package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2023/1/10
 */
public final class SchemaUtils {
    private SchemaUtils() {
    }

    /**
     * 给定{@link Schema}, 从{@code input} 中读取并反序列化pojo
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void write(Output output, Object target, Schema schema) {
        if (Objects.isNull(schema)) {
            Class typeClass = target.getClass();
            Runtime.getSchema(typeClass).write(output, target);
        } else {
            schema.write(output, target);
        }
    }
}
