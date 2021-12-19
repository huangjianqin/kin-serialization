package org.kin.kinbuffer.schema;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.util.Map;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
@SuppressWarnings("rawtypes")
public class MapSchema<K, V> extends NestSchema<Map<K, V>> {
    private final MapFactory mapFactory;
    private final Class<K> keyClass;
    private final Class<V> valueClass;
    private final Schema valueSchema;

    public MapSchema(MapFactory mapFactory, Class<K> keyClass, Class<V> valueClass) {
        this(mapFactory, keyClass, null, valueClass, null);
    }

    public MapSchema(Schema keySchema, Schema valueSchema, MapFactory mapFactory) {
        this(mapFactory, null, keySchema, null, valueSchema);
    }

    public MapSchema(MapFactory mapFactory, Class<K> keyClass, Schema keySchema, Class<V> valueClass, Schema valueSchema) {
        super(keySchema);
        this.mapFactory = mapFactory;
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.valueSchema = valueSchema;
    }

    @Override
    public Map<K, V> newMessage() {
        return mapFactory.newMessage();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void merge(Input input, Map<K, V> kvMap) {
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            kvMap.put((K) Runtime.read(input, keyClass, schema),
                    (V) Runtime.read(input, valueClass, valueSchema));
        }
    }

    @Override
    public void write(Output output, Map<K, V> kvMap) {
        if(Objects.isNull(kvMap)){
            output.writeInt(0);
            return;
        }

        int size = kvMap.size();
        output.writeInt(size);
        for (Map.Entry<K, V> entry : kvMap.entrySet()) {
            Runtime.write(output, entry.getKey(), schema);
            Runtime.write(output, entry.getValue(), valueSchema);
        }
    }
}
