package org.kin.kinbuffer.schema;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public enum MapFactory {
    Map(HashMap.class) {
        @Override
        public <K, V> Map<K, V> newMessage() {
            return new HashMap();
        }
    },
    SortedMap(TreeMap.class) {
        @Override
        public <K, V> Map<K, V> newMessage() {
            return new TreeMap();
        }
    },
    NavigableMap(TreeMap.class) {
        @Override
        public <K, V> Map<K, V> newMessage() {
            return new TreeMap();
        }
    },
    HashMap(HashMap.class) {
        @Override
        public <K, V> Map<K, V> newMessage() {
            return new HashMap();
        }
    },
    LinkedHashMap(LinkedHashMap.class) {
        @Override
        public <K, V> Map<K, V> newMessage() {
            return new LinkedHashMap();
        }
    },
    TreeMap(TreeMap.class) {
        @Override
        public <K, V> Map<K, V> newMessage() {
            return new TreeMap();
        }
    },
    WeakHashMap(WeakHashMap.class) {
        @Override
        public <K, V> Map<K, V> newMessage() {
            return new WeakHashMap();
        }
    },
    IdentityHashMap(IdentityHashMap.class) {
        @Override
        public <K, V> Map<K, V> newMessage() {
            return new IdentityHashMap();
        }
    },
    Hashtable(Hashtable.class) {
        @Override
        public <K, V> Map<K, V> newMessage() {
            return new Hashtable();
        }
    },
    ConcurrentMap(ConcurrentHashMap.class) {
        @Override
        public <K, V> Map<K, V> newMessage() {
            return new ConcurrentHashMap();
        }
    },
    ConcurrentHashMap(ConcurrentHashMap.class) {
        @Override
        public <K, V> Map<K, V> newMessage() {
            return new ConcurrentHashMap();
        }
    },
    ConcurrentNavigableMap(ConcurrentSkipListMap.class) {
        @Override
        public <K, V> Map<K, V> newMessage() {
            return new ConcurrentSkipListMap();
        }
    },
    ConcurrentSkipListMap(ConcurrentSkipListMap.class) {
        @Override
        public <K, V> Map<K, V> newMessage() {
            return new ConcurrentSkipListMap();
        }
    },
    Properties(Properties.class) {
        @Override
        public <K, V> Map<K, V> newMessage() {
            return (java.util.Map<K, V>) new Properties();
        }
    };

    public static final MapFactory[] VALUES = values();
    public final Class<?> typeClass;

    MapFactory(Class<?> typeClass) {
        this.typeClass = typeClass;
    }

    public Class<?> typeClass() {
        return this.typeClass;
    }

    public abstract <K, V> Map<K, V> newMessage();

    public static MapFactory getFactory(Class<? extends Map<?, ?>> mapType) {
        if(!mapType.getName().startsWith("java.util")){
            return null;
        }
        for (MapFactory mapFactory : VALUES) {
            if (mapFactory.name().equals(mapType.getSimpleName())) {
                return mapFactory;
            }
        }
        return null;
    }
}
