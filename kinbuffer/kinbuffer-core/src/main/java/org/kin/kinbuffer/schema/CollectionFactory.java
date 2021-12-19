package org.kin.kinbuffer.schema;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public enum CollectionFactory {
    Collection(ArrayList.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new ArrayList();
        }
    },
    List(ArrayList.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new ArrayList();
        }
    },
    ArrayList(ArrayList.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new ArrayList();
        }
    },
    LinkedList(LinkedList.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new LinkedList();
        }
    },
    CopyOnWriteArrayList(CopyOnWriteArrayList.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new CopyOnWriteArrayList();
        }
    },
    Stack(Stack.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new Stack();
        }
    },
    Vector(Vector.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new Vector();
        }
    },
    Set(HashSet.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new HashSet();
        }
    },
    HashSet(HashSet.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new HashSet();
        }
    },
    LinkedHashSet(LinkedHashSet.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new LinkedHashSet();
        }
    },
    SortedSet(TreeSet.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new TreeSet();
        }
    },
    NavigableSet(TreeSet.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new TreeSet();
        }
    },
    TreeSet(TreeSet.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new TreeSet();
        }
    },
    ConcurrentSkipListSet(ConcurrentSkipListSet.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new ConcurrentSkipListSet();
        }
    },
    CopyOnWriteArraySet(CopyOnWriteArraySet.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new CopyOnWriteArraySet();
        }
    },
    Queue(LinkedList.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new LinkedList();
        }
    },
    BlockingQueue(LinkedBlockingQueue.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new LinkedBlockingQueue();
        }
    },
    LinkedBlockingQueue(java.util.concurrent.LinkedBlockingQueue.class) {
        @Override
        public <V> java.util.Collection<V> newMessage() {
            return new LinkedBlockingQueue();
        }
    },
    Deque(java.util.LinkedList.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new LinkedList();
        }
    },
    BlockingDeque(java.util.concurrent.LinkedBlockingDeque.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new LinkedBlockingDeque();
        }
    },
    LinkedBlockingDeque(LinkedBlockingDeque.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new LinkedBlockingDeque();
        }
    },
    ArrayBlockingQueue(java.util.concurrent.ArrayBlockingQueue.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new ArrayBlockingQueue(10);
        }
    },
    ArrayDeque(java.util.ArrayDeque.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new ArrayDeque();
        }
    },
    ConcurrentLinkedQueue(java.util.concurrent.ConcurrentLinkedQueue.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new ConcurrentLinkedQueue();
        }
    },
    ConcurrentLinkedDeque(java.util.concurrent.ConcurrentLinkedDeque.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new ConcurrentLinkedDeque();
        }
    },
    PriorityBlockingQueue(PriorityBlockingQueue.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new PriorityBlockingQueue();
        }
    },
    PriorityQueue(java.util.PriorityQueue.class) {
        @Override
        public <V> Collection<V> newMessage() {
            return new PriorityQueue();
        }
    };

    public static final CollectionFactory[] VALUES = values();
    public final Class<?> typeClass;

    private CollectionFactory(Class<?> typeClass) {
        this.typeClass = typeClass;
    }

    public Class<?> typeClass() {
        return this.typeClass;
    }

    public abstract <V> Collection<V> newMessage();

    public static CollectionFactory getFactory(Class<? extends Collection<?>> clazz) {
        if(!clazz.getName().startsWith("java.util")){
            return null;
        }
        for (CollectionFactory collectionFactory : VALUES) {
            if(collectionFactory.name().equals(clazz.getSimpleName())){
                return collectionFactory;
            }
        }
        return null;
    }
}
