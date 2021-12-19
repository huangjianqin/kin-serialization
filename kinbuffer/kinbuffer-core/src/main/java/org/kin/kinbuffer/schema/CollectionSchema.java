package org.kin.kinbuffer.schema;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.util.Collection;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
@SuppressWarnings("rawtypes")
public class CollectionSchema<V> extends NestSchema<Collection<V>> {
    private final CollectionFactory collectionFactory;
    private final Class<V> itemClass;

    public CollectionSchema(CollectionFactory collectionFactory, Class<V> itemClass) {
        super(null);
        this.collectionFactory = collectionFactory;
        this.itemClass = itemClass;
    }

    public CollectionSchema(Schema schema, CollectionFactory collectionFactory) {
        super(schema);
        this.collectionFactory = collectionFactory;
        this.itemClass = null;
    }

    @Override
    public Collection<V> newMessage() {
        return collectionFactory.newMessage();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void merge(Input input, Collection<V> vs) {
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            vs.add((V) Runtime.read(input, itemClass, schema));
        }
    }

    @Override
    public void write(Output output, Collection<V> vs) {
        if(Objects.isNull(vs)){
            output.writeInt(0);
            return;
        }

        int size = vs.size();
        output.writeInt(size);
        for (V v : vs) {
            Runtime.write(output, v, schema);
        }
    }
}
