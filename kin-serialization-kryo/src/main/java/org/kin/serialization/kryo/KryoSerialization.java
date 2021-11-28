package org.kin.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import io.netty.buffer.ByteBuf;
import org.kin.framework.concurrent.FastThreadLocal;
import org.kin.framework.utils.Extension;
import org.kin.serialization.Serialization;
import org.kin.serialization.SerializationType;
import org.kin.serialization.kryo.io.Inputs;
import org.kin.serialization.kryo.io.NioBufOutput;
import org.kin.serialization.kryo.io.Outputs;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.nio.ByteBuffer;

/**
 * Created by huangjianqin on 2019/5/29.
 */
@Extension(value = "kryo", code = 2)
public class KryoSerialization implements Serialization {
    /**
     * 解决多线程访问问题
     * 1.池化,
     * 2.ThreadLocal
     */
    private static final FastThreadLocal<Kryo> KRYO_POOL = new FastThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            for (Class<?> registeredClass : Kryos.getJavaSerializerTypes()) {
                kryo.addDefaultSerializer(registeredClass, JavaSerializer.class);
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

    @Override
    public byte[] serialize(Object target) {
        Kryo kryo = KRYO_POOL.get();
        Output output = Outputs.getOutput();
        try {
            kryo.writeObject(output, target);
            return output.toBytes();
        } finally {
            Outputs.clearOutput(output);
        }
    }

    @Override
    public <T> ByteBuffer serialize(ByteBuffer buffer, T target) {
        Kryo kryo = KRYO_POOL.get();
        ByteBufferOutput output = Outputs.getByteBufferOutput(buffer);
        kryo.writeObject(output, target);
        return output.getByteBuffer();
    }

    @Override
    public <T> void serialize(ByteBuf byteBuf, T target) {
        Kryo kryo = KRYO_POOL.get();
        NioBufOutput output = Outputs.getByteBufferOutput(byteBuf);
        kryo.writeObject(output, target);
        output.fixByteBufWriteIndex();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> targetClass) {
        Kryo kryo = KRYO_POOL.get();
        Input input = Inputs.getInput(bytes);
        return kryo.readObject(input, targetClass);
    }

    @Override
    public <T> T deserialize(ByteBuffer buffer, Class<T> targetClass) {
        Kryo kryo = KRYO_POOL.get();
        Input input = Inputs.getInput(buffer);
        return kryo.readObject(input, targetClass);
    }

    @Override
    public <T> T deserialize(ByteBuf buffer, Class<T> targetClass) {
        Kryo kryo = KRYO_POOL.get();
        Input input = Inputs.getInput(buffer);
        return kryo.readObject(input, targetClass);
    }

    @Override
    public int type() {
        return SerializationType.KRYO.getCode();
    }
}
