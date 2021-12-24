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
public final class MessageCollectionSchema<V> implements Schema<Collection<V>> {
    private final CollectionFactory<?> collectionFactory;
    private final Class<V> typeClass;
    @Nullable
    private Schema schema;

    public MessageCollectionSchema(CollectionFactory<?> collectionFactory, Class<V> typeClass) {
        this(collectionFactory, typeClass, null);
    }

    public MessageCollectionSchema(CollectionFactory<?> collectionFactory, Class<V> typeClass, Schema schema) {
        this.collectionFactory = collectionFactory;
        this.typeClass = typeClass;
        this.schema = schema;
    }

    /**
     * lazy init schema
     */
    private void tryLazyInitSchema(){
        if (Objects.isNull(schema)) {
            schema = Runtime.getSchema(typeClass);
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
