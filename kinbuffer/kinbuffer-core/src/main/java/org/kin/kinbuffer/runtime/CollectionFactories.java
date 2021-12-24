package org.kin.kinbuffer.runtime;

import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.ExtensionLoader;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;

/**
 * 集合工厂类管理
 * @author huangjianqin
 * @date 2021/12/18
 */
public final class CollectionFactories {
    private CollectionFactories(){}

    /** key -> collection class, value -> 集合工厂 */
    private static volatile Map<Class<?>, CollectionFactory<?>> FACTORIES;
    
    static {
        Map<Class<?>, CollectionFactory<?>> factories = new HashMap<>();

        //内置
        factories.put(Collection.class, ArrayList::new);
        factories.put(List.class, ArrayList::new);
        factories.put(ArrayList.class, ArrayList::new);
        factories.put(LinkedList.class, LinkedList::new);
        factories.put(CopyOnWriteArrayList.class, CopyOnWriteArrayList::new);
        factories.put(Stack.class, Stack::new);
        factories.put(Vector.class, Vector::new);
        factories.put(Set.class, HashSet::new);
        factories.put(HashSet.class, HashSet::new);
        factories.put(LinkedHashSet.class, LinkedHashSet::new);
        factories.put(SortedSet.class, TreeSet::new);
        factories.put(NavigableSet.class, TreeSet::new);
        factories.put(TreeSet.class, TreeSet::new);
        factories.put(Queue.class, LinkedList::new);
        factories.put(BlockingQueue.class, LinkedBlockingQueue::new);
        factories.put(Deque.class, LinkedBlockingDeque::new);
        factories.put(BlockingDeque.class, LinkedList::new);
        factories.put(ConcurrentSkipListSet.class, ConcurrentSkipListSet::new);
        factories.put(CopyOnWriteArraySet.class, CopyOnWriteArraySet::new);
        factories.put(LinkedBlockingQueue.class, LinkedBlockingQueue::new);
        factories.put(LinkedBlockingDeque.class, LinkedBlockingDeque::new);
        factories.put(ArrayBlockingQueue.class, () -> new ArrayBlockingQueue<>(10));
        factories.put(ArrayDeque.class, ArrayDeque::new);
        factories.put(ConcurrentLinkedQueue.class, ConcurrentLinkedQueue::new);
        factories.put(ConcurrentLinkedDeque.class, ConcurrentLinkedDeque::new);
        factories.put(PriorityBlockingQueue.class, PriorityBlockingQueue::new);
        factories.put(PriorityQueue.class, PriorityQueue::new);

        //通过spi加载
        for (CollectionFactory<?> factory : ExtensionLoader.common().getExtensions(CollectionFactory.class)) {
            List<Type> actualTypes = ClassUtils.getSuperInterfacesGenericActualTypes(CollectionFactory.class, factory.getClass());
            factories.put((Class<?>) actualTypes.get(0), factory);
        }

        FACTORIES = factories;
    }

    /**
     * 根据类型获取集合工厂
     */
    public static CollectionFactory<?> getFactory(Class<?> type) {
        // 这里之所以要遍历该类所有父类和实现接口, 因为对于动态类型(Object)处理时, 写入一个List,
        // 比如java.util.Arrays$ArrayList, 可以正常write, 但我们无法反序列成java.util.Arrays$ArrayList,
        // 不过, 我们可以序列化成java.util.ArrayList, 只要内容一致即可
        // 获取所有继承父类(包含自己)
        List<Class<?>> classes = ClassUtils.getAllClasses(type);
        for (Class<?> claxx : classes) {
            CollectionFactory<?> ret = FACTORIES.get(claxx);
            if (Objects.nonNull(ret)) {
                return ret;
            }

            // 获取所有实现接口
            for (Class<?> interfaceClass : claxx.getInterfaces()) {
                ret = FACTORIES.get(interfaceClass);
                if (Objects.nonNull(ret)) {
                    return ret;
                }
            }
        }
        throw new IllegalArgumentException(String.format("can't not find collection factory for type '%s'", type.getCanonicalName()));
    }

    /**
     * 暴露给user, 注册集合工厂
     */
    public static synchronized void register(CollectionFactory<?> factory){
        List<Type> actualTypes = ClassUtils.getSuperInterfacesGenericActualTypes(CollectionFactory.class, factory.getClass());
        register((Class<?>) actualTypes.get(0), factory);
    }

    /**
     * 暴露给user, 注册集合工厂
     */
    public static synchronized void register(Class<?> type, CollectionFactory<?> factory){
        if(!Collection.class.isAssignableFrom(type)){
            throw new IllegalArgumentException(String.format("type '%s' is not a collection", type.getCanonicalName()));
        }
        Map<Class<?>, CollectionFactory<?>> factories = new HashMap<>(FACTORIES);
        factories.put(type, factory);
        FACTORIES = factories;
    }
}
