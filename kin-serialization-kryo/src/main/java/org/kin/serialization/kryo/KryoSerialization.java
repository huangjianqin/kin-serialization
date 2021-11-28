package org.kin.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import io.netty.buffer.ByteBuf;
import org.kin.framework.concurrent.FastThreadLocal;
import org.kin.framework.utils.Extension;
import org.kin.serialization.AbstractSerialization;
import org.kin.serialization.SerializationType;
import org.kin.serialization.kryo.io.Inputs;
import org.kin.serialization.kryo.io.ByteBufOutput;
import org.kin.serialization.kryo.io.Outputs;
import org.kin.transport.netty.utils.ByteBufUtils;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.nio.ByteBuffer;

/**
 * Created by huangjianqin on 2019/5/29.
 */
@Extension(value = "kryo", code = 2)
public final class KryoSerialization extends AbstractSerialization {
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
        return kryo.readObject(input, targetClass);
    }

    @Override
    public int type() {
        return SerializationType.KRYO.getCode();
    }
}
