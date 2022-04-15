package org.kin.kinbuffer.runtime;

import org.kin.framework.collection.CollectionFactories;
import org.kin.framework.collection.CollectionFactory;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
@SuppressWarnings("rawtypes")
final class MessageCollectionSchema<V> extends PolymorphicSchema<Collection<V>> {
    /** 集合类型 */
    private final Class<?> collectionType;
    /** collection工厂 */
    private final CollectionFactory<?> collectionFactory;
    /** item类型 */
    private final Class<V> itemType;
    /** item schema, 如果为null, 则是pojo, lazy init */
    @Nullable
    private Schema schema;

    MessageCollectionSchema(Class<?> collectionType, Class<V> itemType) {
        this(collectionType, itemType, null);
    }

    MessageCollectionSchema(Class<?> collectionType, Class<V> itemType, Schema schema) {
        this.collectionType = collectionType;
        this.collectionFactory = CollectionFactories.instance().getFactory(collectionType);
        this.itemType = itemType;
        this.schema = schema;
    }

    /**
     * lazy init schema
     */
    private void tryLazyInitSchema() {
        if (Objects.isNull(schema)) {
            schema = Runtime.getSchema(itemType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> read(Input input) {
        tryLazyInitSchema();
        int size = input.readInt32();
        if (size > 0) {
            Collection<V> collection = (Collection<V>) collectionFactory.newCollection();
            for (int i = 0; i < size; i++) {
                collection.add((V) Runtime.read(input, schema));
            }
            return collection;
        } else {
            //空collection, 尝试赋值空collection实例, 目的是为了减少collection实例创建
            if (List.class.equals(collectionType)) {
                return Collections.emptyList();
            } else if (Set.class.equals(collectionType)) {
                return Collections.emptySet();
            } else if (NavigableSet.class.equals(collectionType)) {
                return Collections.emptyNavigableSet();
            } else if (SortedSet.class.equals(collectionType)) {
                return Collections.emptySortedSet();
            } else {
                return (Collection<V>) collectionFactory.newCollection();
            }
        }
    }

    @Override
    public void write(Output output, Collection<V> vs) {
        tryLazyInitSchema();
        if (Objects.isNull(vs)) {
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
