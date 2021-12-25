package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
@SuppressWarnings("rawtypes")
final class MessageMapSchema<K, V> implements Schema<Map<K, V>> {
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

    MessageMapSchema(MapFactory<?> mapFactory, Class<K> keyClass, Schema keySchema, Class<V> valueClass, Schema valueSchema) {
        this.mapFactory = mapFactory;
        this.keyClass = keyClass;
        this.keySchema = keySchema;
        this.valueClass = valueClass;
        this.valueSchema = valueSchema;
    }

    /**
     * lazy init schema
     */
    private void tryLazyInitSchema(){
        if (Objects.isNull(keySchema)) {
            keySchema = Runtime.getSchema(keyClass);
        }

        if (Objects.isNull(valueSchema)) {
            valueSchema = Runtime.getSchema(valueClass);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<K, V> newMessage() {
        return (Map<K, V>) mapFactory.newMap();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void merge(Input input, Map<K, V> kvMap) {
        tryLazyInitSchema();
        int size = input.readInt32();
        for (int i = 0; i < size; i++) {
            kvMap.put((K) Runtime.read(input, keySchema),
                    (V) Runtime.read(input, valueSchema));
        }
    }

    @Override
    public void write(Output output, Map<K, V> kvMap) {
        tryLazyInitSchema();
        if(Objects.isNull(kvMap)){
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
