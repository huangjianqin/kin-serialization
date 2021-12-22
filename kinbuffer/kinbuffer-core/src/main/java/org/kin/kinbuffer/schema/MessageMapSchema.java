package org.kin.kinbuffer.schema;

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
public class MessageMapSchema<K, V> implements Schema<Map<K, V>> {
    private final MapFactory mapFactory;
    private final Class<K> keyClass;
    @Nullable
    private Schema keySchema;
    private final Class<V> valueClass;
    @Nullable
    private Schema valueSchema;

    public MessageMapSchema(MapFactory mapFactory, Class<K> keyClass, Schema keySchema, Class<V> valueClass, Schema valueSchema) {
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

    @Override
    public Map<K, V> newMessage() {
        return mapFactory.newMessage();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void merge(Input input, Map<K, V> kvMap) {
        tryLazyInitSchema();
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            kvMap.put((K) Runtime.read(input, keySchema),
                    (V) Runtime.read(input, valueSchema));
        }
    }

    @Override
    public void write(Output output, Map<K, V> kvMap) {
        tryLazyInitSchema();
        if(Objects.isNull(kvMap)){
            output.writeInt(0);
            return;
        }

        int size = kvMap.size();
        output.writeInt(size);
        for (Map.Entry<K, V> entry : kvMap.entrySet()) {
            Runtime.write(output, entry.getKey(), keySchema);
            Runtime.write(output, entry.getValue(), valueSchema);
        }
    }
}
