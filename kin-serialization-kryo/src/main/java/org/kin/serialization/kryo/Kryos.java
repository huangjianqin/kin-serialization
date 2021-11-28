package org.kin.serialization.kryo;

import org.kin.framework.collection.ConcurrentHashSet;

import java.util.Set;

/**
 * 要注意的是关掉了对存在循环引用的类型的支持, 如果一定要序列化/反序列化循环引用的类型,
 * 可以通过 {@link #useJavaSerializer(Class)} 设置该类型使用Java的序列化/反序列化机制,
 * 对性能有一点影响, 但只是影响一个'点', 不影响'面'.
 *
 * @author huangjianqin
 * @date 2020/11/30
 */
public final class Kryos {
    private Kryos() {
    }

    /** 缓存使用java默认序列化的class */
    private static final ConcurrentHashSet<Class<?>> JAVA_SERIALIZER_TYPES = new ConcurrentHashSet<>();

    static{
        useJavaSerializer(Throwable.class);
    }

    /**
     * 指定{@code clazz}使用java内置的序列化机制
     * 注意!! 这非常消耗性能, 应该尽可能避免使用
     */
    public static void useJavaSerializer(Class<?> clazz) {
        JAVA_SERIALIZER_TYPES.add(clazz);
    }

    public static Set<Class<?>> getJavaSerializerTypes() {
        return JAVA_SERIALIZER_TYPES;
    }
}
