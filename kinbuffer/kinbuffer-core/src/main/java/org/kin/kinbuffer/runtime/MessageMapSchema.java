package org.kin.kinbuffer.runtime;

import org.kin.framework.collection.MapFactories;
import org.kin.framework.collection.MapFactory;
import org.kin.framework.concurrent.FastThreadLocal;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
@SuppressWarnings("rawtypes")
final class MessageMapSchema<K, V> extends PolymorphicSchema<Map<K, V>> {
    private static final FastThreadLocal<MessageMapSchema> CACHE = new FastThreadLocal<MessageMapSchema>() {
        @Override
        protected MessageMapSchema initialValue(){
            return new MessageMapSchema<>(null, null, null, null, null);
        }
    };

    /**
     * 从thread local获取{@link MessageMapSchema}
     * 针对动态类型优化, 减少{@link MessageMapSchema}分配
     */
    static MessageMapSchema fromCache(Class<?> mapType, Class<?> keyClass, Schema keySchema, Class<?> valueClass, Schema valueSchema){
        MessageMapSchema mapSchema = CACHE.get();
        mapSchema.mapType = mapType;
        mapSchema.mapFactory = MapFactories.instance().getFactory(mapType);
        mapSchema.keyClass = keyClass;
        mapSchema.keySchema = keySchema;
        mapSchema.valueClass = valueClass;
        mapSchema.valueSchema = valueSchema;
        return mapSchema;
    }

    /** map类型 */
    private Class<?> mapType;
    /** map工厂 */
    private MapFactory<?> mapFactory;
    /** key类型 */
    private Class<K> keyClass;
    /** key schema, 如果为null, 则是pojo, lazy init */
    @Nullable
    private Schema keySchema;
    /** value类型 */
    private Class<V> valueClass;
    /** value schema, 如果为null, 则是pojo, lazy init */
    @Nullable
    private Schema valueSchema;

    MessageMapSchema(Class<?> mapType, Class<K> keyClass, Schema keySchema, Class<V> valueClass, Schema valueSchema) {
        this.mapType = mapType;
        if (Objects.nonNull(mapType)) {
            this.mapFactory = MapFactories.instance().getFactory(mapType);
        }
        this.keyClass = keyClass;
        this.keySchema = keySchema;
        this.valueClass = valueClass;
        this.valueSchema = valueSchema;
    }

    /**
     * lazy init schema
     */
    private void tryLazyInitSchema() {
        if (Objects.isNull(keySchema)) {
            keySchema = Runtime.getSchema(keyClass);
        }

        if (Objects.isNull(valueSchema)) {
            valueSchema = Runtime.getSchema(valueClass);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<K, V> read(Input input) {

        tryLazyInitSchema();
        int size = input.readInt32();
        if(size > 0){
            Map<K, V> map = (Map<K, V>) mapFactory.newMap();
            for (int i = 0; i < size; i++) {
                map.put((K) SchemaUtils.read(input, keySchema),
                        (V) SchemaUtils.read(input, valueSchema));
            }
            return map;
        }
        else{
            if(Map.class.equals(mapType)){
                return Collections.emptyMap();
            }else if(NavigableMap.class.equals(mapType)){
                return Collections.emptyNavigableMap();
            }
            else if(SortedMap.class.equals(mapType)){
                return Collections.emptySortedMap();
            }
            else{
                return (Map<K, V>) mapFactory.newMap();
            }
        }
    }

    @Override
    public void write(Output output, Map<K, V> kvMap) {
        tryLazyInitSchema();
        if (Objects.isNull(kvMap)) {
            output.writeInt32(0);
            return;
        }

        int size = kvMap.size();
        output.writeInt32(size);
        for (Map.Entry<K, V> entry : kvMap.entrySet()) {
            SchemaUtils.write(output, entry.getKey(), keySchema);
            SchemaUtils.write(output, entry.getValue(), valueSchema);
        }
    }
}
