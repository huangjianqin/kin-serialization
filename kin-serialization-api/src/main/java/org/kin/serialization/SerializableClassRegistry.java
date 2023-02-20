package org.kin.serialization;

import java.util.HashMap;
import java.util.Map;

/**
 * 暴露给使用者的提前注册class接口
 * 仅适用于kryo和kinbuffer
 * @author huangjianqin
 * @date 2023/2/20
 */
public final class SerializableClassRegistry {
    private static final Map<Class<?>, Object> REGISTRATIONS = new HashMap<>();

    /**
     * 仅仅注册类, 但不注册serializer
     *
     * @param clazz object type
     */
    public static void registerClass(Class<?> clazz) {
        registerClass(clazz, null);
    }

    /**
     * 同时注册类和serializer
     *
     * @param clazz object type
     * @param serializer object serializer
     */
    public static void registerClass(Class<?> clazz, Object serializer) {
        if (clazz == null) {
            throw new IllegalArgumentException("class cannot be null!");
        }
        REGISTRATIONS.put(clazz, serializer);
    }

    /**
     * 获取的注册的类和serializer
     *
     * @return class serializer
     * */
    public static Map<Class<?>, Object> getRegisteredClasses() {
        return REGISTRATIONS;
    }
}
