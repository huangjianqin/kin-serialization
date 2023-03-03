package org.kin.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import io.netty.buffer.ByteBuf;
import org.kin.framework.concurrent.FastThreadLocal;
import org.kin.framework.io.ByteBufferUtils;
import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.Extension;
import org.kin.serialization.AbstractSerialization;
import org.kin.serialization.SerializableClassRegistry;
import org.kin.serialization.SerializationType;
import org.kin.serialization.kryo.io.Inputs;
import org.kin.serialization.kryo.io.ByteBufOutput;
import org.kin.serialization.kryo.io.Outputs;
import org.kin.transport.netty.utils.ByteBufUtils;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by huangjianqin on 2019/5/29.
 */
@Extension(value = "kryo", code = 2)
public final class KryoSerialization extends AbstractSerialization {
    /** 使用者注册消息id从128开始 */
    private static final int MIN_ID = 128;

    /**
     * 解决多线程访问问题
     * 1.池化,Pool对象
     * 2.ThreadLocal
     */
    private static final FastThreadLocal<Kryo> KRYO_POOL = new FastThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            for (Class<?> registeredClass : Kryos.getJavaSerializerTypes()) {
                kryo.addDefaultSerializer(registeredClass, JavaSerializer.class);
            }

            //user custom
            int userSerializerId = MIN_ID;
            for (Map.Entry<Class<?>, Object> entry : SerializableClassRegistry.getRegisteredClasses().entrySet()) {
                Class<?> claxx = entry.getKey();
                Object serializer = entry.getValue();
                if (Objects.nonNull(serializer)) {
                    kryo.register(claxx, (Serializer) serializer);
                }
                else{
                    kryo.register(claxx, userSerializerId++);
                }
            }

            //设置初始化策略, 如果没有默认无参构造器, 那么就需要设置此项,使用此策略构造一个无参构造器
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            //设置是否注册全限定名,
            kryo.setRegistrationRequired(false);
            /*
                引用, 对A对象序列化时, 默认情况下kryo会在每个成员对象第一次序列化时写入一个数字, 该数字逻辑上就代表了对该成员对象的引用, 如果后续有引用指向该成员对象,
                则直接序列化之前存入的数字即可, 而不需要再次序列化对象本身. 这种默认策略对于成员存在互相引用的情况较有利, 否则就会造成空间浪费
                (因为没序列化一个成员对象, 都多序列化一个数字), 通常情况下可以将该策略关闭, kryo.setReferences(false);
             */
            kryo.setReferences(false);
            return kryo;
        }
    };

    /**
     * 注册javaSE常用类
     */
    private static void registerClass(Kryo kryo){
        int i = 1;
        kryo.register(Object.class, i++);
        kryo.register(ArrayList.class, i++);
        kryo.register(LinkedList.class, i++);
        kryo.register(CopyOnWriteArrayList.class, i++);
        kryo.register(Stack.class, i++);
        kryo.register(Vector.class, i++);
        kryo.register(HashSet.class, i++);
        kryo.register(LinkedHashSet.class, i++);
        kryo.register(TreeSet.class, i++);
        kryo.register(BitSet.class, i++);
        kryo.register(ConcurrentSkipListSet.class, i++);
        kryo.register(CopyOnWriteArraySet.class, i++);
        kryo.register(LinkedBlockingQueue.class, i++);
        kryo.register(LinkedBlockingDeque.class, i++);
        kryo.register(ArrayBlockingQueue.class, i++);
        kryo.register(ArrayDeque.class, i++);
        kryo.register(ConcurrentLinkedQueue.class, i++);
        kryo.register(ConcurrentLinkedDeque.class, i++);
        kryo.register(PriorityBlockingQueue.class, i++);
        kryo.register(PriorityQueue.class, i++);
        //注册Singleton或Empty Collection
        kryo.register(ClassUtils.getClass("java.util.Arrays$ArrayList"), i++);
        kryo.register(ClassUtils.getClass("java.util.Collections$SingletonList"), i++);
        kryo.register(ClassUtils.getClass("java.util.Collections$SingletonSet"), i++);
        kryo.register(ClassUtils.getClass("java.util.Collections$EmptyList"), i++);
        kryo.register(ClassUtils.getClass("java.util.Collections$EmptySet"), i++);
        kryo.register(ClassUtils.getClass("java.util.Collections$UnmodifiableNavigableSet$EmptyNavigableSet"), i++);
        kryo.register(ClassUtils.getClass("java.util.Collections$UnmodifiableCollection"), i++);
        kryo.register(ClassUtils.getClass("java.util.Collections$UnmodifiableList"), i++);
        kryo.register(ClassUtils.getClass("java.util.Collections$UnmodifiableSet"), i++);
        kryo.register(ClassUtils.getClass("java.util.Collections$UnmodifiableNavigableSet"), i++);
        //注册基础类型二维数组
        kryo.register(byte[].class, i++);
        kryo.register(byte[][].class, i++);
        kryo.register(char[].class, i++);
        kryo.register(char[][].class, i++);
        kryo.register(short[].class, i++);
        kryo.register(short[][].class, i++);
        kryo.register(int[].class, i++);
        kryo.register(int[][].class, i++);
        kryo.register(float[].class, i++);
        kryo.register(float[][].class, i++);
        kryo.register(long[].class, i++);
        kryo.register(long[][].class, i++);
        kryo.register(double[].class, i++);
        kryo.register(double[][].class, i++);
        kryo.register(Object[].class, i++);
        kryo.register(Object[][].class, i++);
        kryo.register(Byte[].class, i++);
        kryo.register(Byte[][].class, i++);
        kryo.register(Character[].class, i++);
        kryo.register(Character[][].class, i++);
        kryo.register(Short[].class, i++);
        kryo.register(Short[][].class, i++);
        kryo.register(Integer[].class, i++);
        kryo.register(Integer[][].class, i++);
        kryo.register(Float[].class, i++);
        kryo.register(Float[][].class, i++);
        kryo.register(Long[].class, i++);
        kryo.register(Long[][].class, i++);
        kryo.register(Double[].class, i++);
        kryo.register(Double[][].class, i++);
        kryo.register(HashMap.class, i++);
        kryo.register(TreeMap.class, i++);
        kryo.register(LinkedHashMap.class, i++);
        kryo.register(WeakHashMap.class, i++);
        kryo.register(IdentityHashMap.class, i++);
        kryo.register(Hashtable.class, i++);
        kryo.register(ConcurrentHashMap.class, i++);
        kryo.register(ConcurrentSkipListMap.class, i++);
        kryo.register(Properties.class, i++);
        kryo.register(ClassUtils.getClass("java.util.Collections$SingletonMap"), i++);
        kryo.register(ClassUtils.getClass("java.util.Collections$EmptyMap"), i++);
        kryo.register(ClassUtils.getClass("java.util.Collections$UnmodifiableNavigableMap$EmptyNavigableMap"), i++);
        kryo.register(ClassUtils.getClass("java.util.Collections$UnmodifiableNavigableMap"), i++);
        kryo.register(ClassUtils.getClass("java.util.Collections$UnmodifiableMap"), i++);
    }

    @Override
    protected byte[] serialize0(Object target) {
        Kryo kryo = KRYO_POOL.get();
        Output output = Outputs.getOutput();
        try {
            kryo.writeObject(output, target);
            return output.toBytes();
        } finally {
            Outputs.resetOutput(output);
        }
    }

    @Override
    protected <T> ByteBuffer serialize0(ByteBuffer byteBuffer, T target) {
        Kryo kryo = KRYO_POOL.get();
        ByteBufferOutput output = Outputs.getByteBufferOutput(byteBuffer);
        kryo.writeObject(output, target);
        return output.getByteBuffer();
    }

    @Override
    protected <T> void serialize0(ByteBuf byteBuf, T target) {
        Kryo kryo = KRYO_POOL.get();
        ByteBufOutput output = Outputs.getByteBufferOutput(byteBuf);
        kryo.writeObject(output, target);
        output.fixByteBufWriteIndex();
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> targetClass) {
        Kryo kryo = KRYO_POOL.get();
        Input input = Inputs.getInput(bytes);
        return kryo.readObject(input, targetClass);
    }

    @Override
    protected <T> T deserialize0(ByteBuffer byteBuffer, Class<T> targetClass) {
        Kryo kryo = KRYO_POOL.get();
        Input input = Inputs.getInput(byteBuffer);
        return kryo.readObject(input, targetClass);
    }

    @Override
    protected <T> T deserialize0(ByteBuf byteBuf, Class<T> targetClass) {
        Kryo kryo = KRYO_POOL.get();
        Input input = Inputs.getInput(byteBuf);
        T deserialize = kryo.readObject(input, targetClass);
        ByteBufUtils.fixByteBufReadIndex(byteBuf, byteBuf.nioBuffer());
        return deserialize;
    }

    @Override
    public int type() {
        return SerializationType.KRYO.getCode();
    }
}
