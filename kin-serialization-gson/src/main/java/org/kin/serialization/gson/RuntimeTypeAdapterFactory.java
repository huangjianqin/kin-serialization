package org.kin.serialization.gson;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.kin.framework.collection.CopyOnWriteMap;
import org.kin.framework.utils.ClassUtils;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 针对不确定类型的优化处理, 所谓不确定类型, 即接口, 抽象类或者{@link Object}, 但目前还不支持{@link Object}.
 * 因为gson强制使用{@link com.google.gson.internal.bind.ObjectTypeAdapter}来处理{@link Object}实例, 其逻辑是会将反序列化成json map
 *
 * 根本实现原理是序列化时带上实际类型信息
 *
 * 参考<a href="https://github.com/google/gson/blob/main/extras/src/main/java/com/google/gson/typeadapters/RuntimeTypeAdapterFactory.java">RuntimeTypeAdapterFactory</a>.
 *
 * @author huangjianqin
 * @date 2023/7/16
 */
public final class RuntimeTypeAdapterFactory implements TypeAdapterFactory {
    /** gson中标识class信息的字段key */
    private static final String TYPE_PROPERTY_NAME = "@c";
    /** 单例 */
    public static final TypeAdapterFactory INSTANCE = new RuntimeTypeAdapterFactory();

    /** 注册到的{@link Gson}实例 */
    private Gson gson;
    /** {@link JsonElement}对应的{@link TypeAdapter}实例 */
    private TypeAdapter<JsonElement> jsonElementAdapter;
    /** key -> class, value -> 该类型对应的{@link TypeAdapter}实例 */
    private final Map<Class<?>, TypeAdapter<?>> typeToTypeAdapter = new CopyOnWriteMap<>(LinkedHashMap::new);

    private RuntimeTypeAdapterFactory() {
    }

    /**
     * 初始化{@code type}对应的{@link TypeAdapter}实例
     * @param type  class
     */
    private TypeAdapter<?> initTypeAdapter(Class<?> type) {
        TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get(type));
        typeToTypeAdapter.put(type, delegate);
        return delegate;
    }

    @Override
    public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
        if (type == null) {
            return null;
        }
        Class<?> rawType = type.getRawType();
        int modifiers = rawType.getModifiers();

        if(rawType.isArray() || rawType.isPrimitive() ||
                Collection.class.isAssignableFrom(rawType) ||
                Map.class.isAssignableFrom(rawType)){
            //filter array, primitive, collection and map
            return null;
        }

        //暂时拦截不到object类型, gson强制使用内部实现, 即会反序列成json map
        if (!Object.class.equals(rawType) &&
                !Modifier.isAbstract(modifiers) &&
                !Modifier.isInterface(modifiers)) {
            return null;
        }

        if (Objects.isNull(this.gson)) {
            //lazy init belong gson instance
            this.gson = gson;
            this.jsonElementAdapter = gson.getAdapter(JsonElement.class);
        }

        return new TypeAdapter<R>() {
            @SuppressWarnings("unchecked")
            @Override
            public R read(JsonReader in) throws IOException {
                JsonElement jsonElement = jsonElementAdapter.read(in);
                JsonElement labelJsonElement = jsonElement.getAsJsonObject().remove(TYPE_PROPERTY_NAME);

                if (labelJsonElement == null) {
                    throw new JsonParseException("cannot deserialize runtime type because it does not define a field named " + TYPE_PROPERTY_NAME);
                }

                //class name
                String label = labelJsonElement.getAsString();
                //get class
                Class<Object> realType = ClassUtils.getClass(label);
                //get typeAdapter
                TypeAdapter<R> typeAdapter = (TypeAdapter<R>) typeToTypeAdapter.get(realType);
                if (typeAdapter == null) {
                    //lazy init unknown type typeAdapter
                    typeAdapter = (TypeAdapter<R>) initTypeAdapter(realType);
                }

                return typeAdapter.fromJsonTree(jsonElement);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void write(JsonWriter out, R value) throws IOException {
                Class<?> realType = value.getClass();
                //class name
                String label = realType.getName();
                //get typeAdapter
                TypeAdapter<R> typeAdapter = (TypeAdapter<R>) typeToTypeAdapter.get(realType);
                if (typeAdapter == null) {
                    //lazy init unknown type typeAdapter
                    typeAdapter = (TypeAdapter<R>) initTypeAdapter(realType);
                }

                //序列化
                JsonObject jsonObject = typeAdapter.toJsonTree(value).getAsJsonObject();

                //复制并写入类型信息
                JsonObject jsonObjectWithTypeInfo = new JsonObject();
                //overwrite
                jsonObjectWithTypeInfo.add(TYPE_PROPERTY_NAME, new JsonPrimitive(label));

                for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                    jsonObjectWithTypeInfo.add(e.getKey(), e.getValue());
                }
                jsonElementAdapter.write(out, jsonObjectWithTypeInfo);
            }
        }.nullSafe();
    }
}