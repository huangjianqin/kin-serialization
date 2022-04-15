package org.kin.kinbuffer.runtime;

import org.kin.framework.collection.MapFactories;
import org.kin.framework.collection.MapFactory;
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
    /** map类型 */
    private final Class<?> mapType;
    /** map工厂 */
    private final MapFactory<?> mapFactory;
    /** key类型 */
    private final Class<K> keyClass;
    /** key schema, 如果为null, 则是pojo, lazy init */
    @Nullable
    private Schema keySchema;
    /** value类型 */
    private final Class<V> valueClass;
    /** value schema, 如果为null, 则是pojo, lazy init */
    @Nullable
    private Schema valueSchema;

    MessageMapSchema(Class<?> mapType, Class<K> keyClass, Schema keySchema, Class<V> valueClass, Schema valueSchema) {
        this.mapType = mapType;
        this.mapFactory = MapFactories.instance().getFactory(mapType);
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
                map.put((K) Runtime.read(input, keySchema),
                        (V) Runtime.read(input, valueSchema));
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
            Runtime.write(output, entry.getKey(), keySchema);
            Runtime.write(output, entry.getValue(), valueSchema);
        }
    }
}
