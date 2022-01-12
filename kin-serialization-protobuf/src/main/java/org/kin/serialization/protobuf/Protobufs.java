package org.kin.serialization.protobuf;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.protobuf.*;
import com.google.protobuf.util.JsonFormat;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import org.kin.framework.io.ByteBufferInputStream;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.serialization.OutputStreams;
import org.kin.serialization.SerializationException;

import java.io.*;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author huangjianqin
 * @date 2020/11/29
 */
public final class Protobufs {
    private Protobufs() {
    }

    /** parser缓存 */
    private static final Cache<Class<? extends MessageLite>, MessageMarshaller<?>> MARSHALLERS = CacheBuilder.newBuilder().build();

    /** 获取{@link GeneratedMessageV3.Builder}的{@link Supplier}代理 */
    @SuppressWarnings("rawtypes")
    private static final Cache<Class<?>, Supplier<GeneratedMessageV3.Builder>> BUILDER_CACHE = CacheBuilder.newBuilder().build();

    private static final ExtensionRegistryLite GLOBAL_REGISTRY = ExtensionRegistryLite.getEmptyRegistry();

    /**
     * 注册class parser
     */
    public static <T extends MessageLite> void register(T defaultInstance) {
        MARSHALLERS.put(defaultInstance.getClass(), new ParserBaseMessageMarshaller<>(defaultInstance));
    }

    /**
     * protobuf message serialize
     */
    public static byte[] serialize(Object target) {
        ByteArrayOutputStream baos = OutputStreams.getByteArrayOutputStream();
        try {
            serialize(baos, target);
            return baos.toByteArray();
        } finally {
            OutputStreams.resetBuf(baos);
        }
    }

    /**
     * protobuf message serialize
     */
    public static void serialize(OutputStream os, Object target) {
        if (!(target instanceof MessageLite)) {
            throw new SerializationException(target.getClass().getName().concat("is not a protobuf object"));
        }

        MessageLite messageLite = (MessageLite) target;
        try {
            messageLite.writeDelimitedTo(os);
            os.flush();
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
    }

    /**
     * protobuf message deserialize
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
            throw new SerializationException(String.format("can't not found %s marshaller", targetClass.getName()));
        }

        return (T) marshaller.parse(bytes);
    }

    /**
     * protobuf deserialize to json
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T deserializeJson(String json, Class<T> messageClass) {
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
     * 获取MessageBuilder
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static GeneratedMessageV3.Builder getMessageBuilder(Class<?> messageClass) throws Exception {
        Supplier<GeneratedMessageV3.Builder> supplier = BUILDER_CACHE.get(messageClass, () -> {
            Method method = messageClass.getMethod("newBuilder");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle methodHandle = lookup.unreflect(method);
            MethodType methodType = methodHandle.type();
            try {
                return (Supplier<GeneratedMessageV3.Builder>) LambdaMetafactory.metafactory(lookup, "get",
                                MethodType.methodType(Supplier.class), methodType.generic(), methodHandle, methodType)
                        .getTarget()
                        .invoke();
            } catch (Throwable e) {
                ExceptionUtils.throwExt(e);
            }
            //理论上不会到这里
            throw new IllegalStateException("encounter unknown error");
        });
        return supplier.get();
    }

    /**
     * protobuf serialize to json
     */
    public static String serializeJson(Object value) {
        JsonFormat.Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();
        try {
            return printer.print((MessageOrBuilder) value);
        } catch (InvalidProtocolBufferException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
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
         * 从{@link Byte[]}解析构建protobuf message
         */
        T parse(byte[] bytes) {
            return parse(new ByteArrayInputStream(bytes));
        }

        /**
         * 从{@link ByteBuffer}解析构建protobuf message
         */
        T parse(ByteBuffer byteBuffer) {
            return parse(new ByteBufferInputStream(byteBuffer));
        }

        /**
         * 从{@link ByteBuf}解析构建protobuf message
         */
        T parse(ByteBuf byteBuf) {
            return parse(new ByteBufInputStream(byteBuf));
        }

        /**
         * 从{@link InputStream}解析构建protobuf message
         */
        abstract T parse(InputStream is);

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
        T parse(InputStream is) {
            try {
                return parser.parseDelimitedFrom(is, GLOBAL_REGISTRY);
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
     * 基于protobuf message parseFrom(InputStream)静态方法
     * 使用lambda代理
     */
    private static final class MethodBaseMessageMarshaller<T extends MessageLite> extends MessageMarshaller<T> {
        /** protobuf message 静态方法parseFrom */
        private Function<InputStream, T> parseFromInputStreamFunc;

        @SuppressWarnings("unchecked")
        MethodBaseMessageMarshaller(Class<T> claxx) {
            super(claxx);
            try {
                Method method = claxx.getMethod("parseFrom", InputStream.class);
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandle methodHandle = lookup.unreflect(method);
                MethodType methodType = methodHandle.type();
                parseFromInputStreamFunc = (Function<InputStream, T>) LambdaMetafactory.metafactory(lookup, "apply",
                                MethodType.methodType(Function.class), methodType.generic(), methodHandle, methodType)
                        .getTarget()
                        .invoke();
            } catch (Throwable throwable) {
                ExceptionUtils.throwExt(throwable);
            }
        }

        @Override
        T parse(InputStream is) {
            return parseFromInputStreamFunc.apply(is);
        }
    }
}
