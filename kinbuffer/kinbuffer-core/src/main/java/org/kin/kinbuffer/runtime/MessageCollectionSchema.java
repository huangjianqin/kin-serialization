package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
@SuppressWarnings("rawtypes")
final class MessageCollectionSchema<V> implements Schema<Collection<V>> {
    /** collection工厂 */
    private final CollectionFactory<?> collectionFactory;
    /** item类型 */
    private final Class<V> itemType;
    /** item schema, 如果为null, 则是pojo, lazy init */
    @Nullable
    private Schema schema;

    MessageCollectionSchema(CollectionFactory<?> collectionFactory, Class<V> itemType) {
        this(collectionFactory, itemType, null);
    }

    MessageCollectionSchema(CollectionFactory<?> collectionFactory, Class<V> itemType, Schema schema) {
        this.collectionFactory = collectionFactory;
        this.itemType = itemType;
        this.schema = schema;
    }

    /**
     * lazy init schema
     */
    private void tryLazyInitSchema(){
        if (Objects.isNull(schema)) {
            schema = Runtime.getSchema(itemType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> newMessage() {
        return (Collection<V>) collectionFactory.newCollection();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void merge(Input input, Collection<V> vs) {
        tryLazyInitSchema();
        int size = input.readInt32();
        for (int i = 0; i < size; i++) {
            vs.add((V) Runtime.read(input, schema));
        }
    }

    @Override
    public void write(Output output, Collection<V> vs) {
        tryLazyInitSchema();
        if(Objects.isNull(vs)){
            output.writeInt32(0);
            return;
        }

        int size = vs.size();
        output.writeInt32(size);
        for (V v : vs) {
            Runtime.write(output, v, schema);
        }
    }
}
