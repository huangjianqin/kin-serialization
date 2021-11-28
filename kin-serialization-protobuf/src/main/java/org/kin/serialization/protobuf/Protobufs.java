package org.kin.serialization.protobuf;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.protobuf.*;
import com.google.protobuf.util.JsonFormat;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.serialization.OutputStreams;
import org.kin.serialization.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * @author huangjianqin
 * @date 2020/11/29
 */
public final class Protobufs {
    private Protobufs() {
    }

    /** parser缓存 */
    private static final Cache<Class<? extends MessageLite>, MessageMarshaller<?>> MARSHALLERS = CacheBuilder.newBuilder().build();
    /** 获取{@link GeneratedMessageV3.Builder}的{@link Method}缓存 */
    private static final Cache<Class<?>, Method> BUILDER_CACHE = CacheBuilder.newBuilder().build();

    private static final ExtensionRegistryLite GLOBAL_REGISTRY = ExtensionRegistryLite.getEmptyRegistry();

    /**
     * 注册class parser
     */
    public static <T extends MessageLite> void register(T defaultInstance) {
        MARSHALLERS.put(defaultInstance.getClass(), new ParserBaseMessageMarshaller<>(defaultInstance));
    }

    /**
     * protobuf serialize
     */
    public static byte[] serialize(Object target){
        if (!(target instanceof MessageLite)) {
            throw new SerializationException(target.getClass().getName().concat("is not a protobuf object"));
        }

        ByteArrayOutputStream baos = OutputStreams.getByteArrayOutputStream();
        MessageLite messageLite = (MessageLite) target;
        try {
            messageLite.writeDelimitedTo(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }finally {
            OutputStreams.resetBuf(baos);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    /**
     * protobuf deserialize
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T deserialize(byte[] bytes, Class<T> targetClass) {
        if (!MessageLite.class.isAssignableFrom(targetClass)) {
            throw new SerializationException(targetClass.getName().concat("is not a protobuf object"));
        }

        MessageMarshaller<?> marshaller = null;
        try {
            marshaller = MARSHALLERS.get((Class<? extends MessageLite>) targetClass, () -> new MethodBaseMessageMarshaller(targetClass));
        } catch (ExecutionException e) {
            ExceptionUtils.throwExt(e);
        }

        if (Objects.isNull(marshaller)) {
            throw new SerializationException(targetClass.getName().concat("does not register"));
        }

        return (T) marshaller.parse(bytes);
    }

    /**
     * protobuf deserialize to json
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T deserializeJson(String json, Class<T> messageClass){
        GeneratedMessageV3.Builder builder;
        try {
            builder = getMessageBuilder(messageClass);
        } catch (Exception e) {
            throw new SerializationException("get google protobuf message builder from " + messageClass.getName() + "failed", e);
        }
        try {
            JsonFormat.parser().merge(json, builder);
        } catch (InvalidProtocolBufferException e) {
            ExceptionUtils.throwExt(e);
        }
        return (T) builder.build();
    }

    /**
     * protobuf serialize to json
     */
    public static String serializeJson(Object value){
        JsonFormat.Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();
        try {
            return printer.print((MessageOrBuilder) value);
        } catch (InvalidProtocolBufferException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    /**
     * 获取MessageBuilder
     */
    @SuppressWarnings("rawtypes")
    private static GeneratedMessageV3.Builder getMessageBuilder(Class<?> requestType) throws Exception {
        Method method = BUILDER_CACHE.get(requestType, () -> requestType.getMethod("newBuilder"));
        return (GeneratedMessageV3.Builder) method.invoke(null);
    }

    //------------------------------------------------------------------------------------------------------------

    /**
     * protobuf message 构建封装
     */
    private static abstract class MessageMarshaller<T extends MessageLite> {
        protected final Class<T> messageClass;

        MessageMarshaller(Class<T> claxx) {
            messageClass = claxx;
        }

        /**
         * 从bytes解析构建protobuf消息
         */
        abstract T parse(byte[] bytes);

        //getter
        Class<T> getMessageClass() {
            return messageClass;
        }
    }

    /**
     * 基于{@link Parser}
     */
    private static final class ParserBaseMessageMarshaller<T extends MessageLite> extends MessageMarshaller<T> {
        /** parser */
        private final Parser<T> parser;
        /** protobuf默认实例 */
        private final T defaultInstance;

        @SuppressWarnings("unchecked")
        ParserBaseMessageMarshaller(T defaultInstance) {
            super((Class<T>) defaultInstance.getClass());
            this.defaultInstance = defaultInstance;
            parser = (Parser<T>) defaultInstance.getParserForType();
        }

        @Override
        T parse(byte[] bytes) {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            try {
                return parser.parseDelimitedFrom(bais, GLOBAL_REGISTRY);
            } catch (InvalidProtocolBufferException e) {
                ExceptionUtils.throwExt(e);
            }

            return null;
        }

        //getter
        public T getMessageProtoInstance() {
            return defaultInstance;
        }
    }

    /**
     * 基于protobuf message parseFrom(byte[])静态方法
     * java反射
     */
    private static final class MethodBaseMessageMarshaller<T extends MessageLite> extends MessageMarshaller<T> {
        /** protobuf message 静态方法parseFrom */
        private Method parseFromStaticMethod;

        MethodBaseMessageMarshaller(Class<T> claxx) {
            super(claxx);
            try {
                parseFromStaticMethod = claxx.getMethod("parseFrom", byte[].class);
            } catch (NoSuchMethodException e) {
                ExceptionUtils.throwExt(e);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        T parse(byte[] bytes) {
            try {
                return (T) parseFromStaticMethod.invoke(null, bytes);
            } catch (IllegalAccessException | InvocationTargetException e) {
                ExceptionUtils.throwExt(e);
            }

            return null;
        }
    }
}