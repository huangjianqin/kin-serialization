package org.kin.kinbuffer.runtime;

import org.kin.framework.collection.CollectionFactories;
import org.kin.framework.collection.CollectionFactory;
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
final class MessageCollectionSchema<V> extends PolymorphicSchema<Collection<V>> {
    private static final FastThreadLocal<MessageCollectionSchema> CACHE = new FastThreadLocal<MessageCollectionSchema>() {
        @Override
        protected MessageCollectionSchema initialValue() {
            return new MessageCollectionSchema<>(null, null);
        }
    };

    /**
     * 从thread local获取{@link MessageCollectionSchema}
     * 针对动态类型优化, 减少{@link MessageCollectionSchema}分配
     */
    static MessageCollectionSchema fromCache(Class<?> collectionType, Class<?> itemType, Schema schema){
        MessageCollectionSchema collectionSchema = CACHE.get();
        collectionSchema.collectionType = collectionType;
        collectionSchema.collectionFactory = CollectionFactories.instance().getFactory(collectionType);
        collectionSchema.itemType = itemType;
        collectionSchema.schema = schema;
        return collectionSchema;
    }

    /** 集合类型 */
    protected Class<?> collectionType;
    /** collection工厂 */
    protected CollectionFactory<?> collectionFactory;
    /** item类型 */
    protected Class<V> itemType;
    /** item schema, 如果为null, 则是pojo, lazy init */
    @Nullable
    protected Schema schema;

    MessageCollectionSchema(Class<?> collectionType, Class<V> itemType) {
        this(collectionType, itemType, null);
    }

    MessageCollectionSchema(Class<?> collectionType, Class<V> itemType, Schema schema) {
        this.collectionType = collectionType;
        if (Objects.nonNull(collectionType)) {
            this.collectionFactory = CollectionFactories.instance().getFactory(collectionType);
        }
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
        int size = input.readVarInt32();
        if (size > 0) {
            Collection<V> collection = (Collection<V>) collectionFactory.newCollection();
            for (int i = 0; i < size; i++) {
                collection.add((V) SchemaUtils.read(input, schema));
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
            output.writeVarInt32(0);
            return;
        }

        int size = vs.size();
        output.writeVarInt32(size);
        for (V v : vs) {
            SchemaUtils.write(output, v, schema);
        }
    }
}
