package org.kin.serialization.hessian2;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.kin.framework.concurrent.FastThreadLocal;
import org.kin.framework.io.ByteBufferInputStream;
import org.kin.framework.io.ScalableByteBufferOutputStream;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.serialization.AbstractSerialization;
import org.kin.serialization.OutputStreams;
import org.kin.serialization.SerializationType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by 健勤 on 2017/2/9.
 */
@Extension(value = "hessian2", code = 3)
public final class Hessian2Serialization extends AbstractSerialization {
    /**
     * thread local output
     * {@link Hessian2Output}内部使用byte[]来缓存部分bytes, 当内部byte[]了, 则会flush到绑定的{@link OutputStream}
     * @see Hessian2Output
     */
    private static final FastThreadLocal<Hessian2Output> THREAD_LOCAL_OUTPUT = new FastThreadLocal<Hessian2Output>() {
        @Override
        protected Hessian2Output initialValue(){
            return new Hessian2Output();
        }
    };

    @Override
    protected byte[] serialize0(Object target) {
        ByteArrayOutputStream baos = OutputStreams.getByteArrayOutputStream();
        try {
            serialize1(baos, target);
            return baos.toByteArray();
        } finally {
            OutputStreams.resetBuf(baos);
        }
    }

    @Override
    protected <T> ByteBuffer serialize0(ByteBuffer byteBuffer, T target) {
        ScalableByteBufferOutputStream ebbos = new ScalableByteBufferOutputStream(byteBuffer);
        serialize1(ebbos, target);
        return ebbos.getSink();
    }

    @Override
    protected <T> void serialize0(ByteBuf byteBuf, T target) {
        serialize1(new ByteBufOutputStream(byteBuf), target);
    }

    private <T> void serialize1(OutputStream os, T target) {
        Hessian2Output output = THREAD_LOCAL_OUTPUT.get();
        try {
            output.init(os);
            output.writeObject(target);
            output.flush();
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        } finally {
            output.reset();
        }
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> targetClass) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return deserialize1(new Hessian2Input(inputStream), targetClass);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    @Override
    protected <T> T deserialize0(ByteBuffer byteBuffer, Class<T> targetClass) {
        return deserialize1(new Hessian2Input(new ByteBufferInputStream(byteBuffer)), targetClass);
    }

    @Override
    protected <T> T deserialize0(ByteBuf byteBuf, Class<T> targetClass) {
        return deserialize1(new Hessian2Input(new ByteBufInputStream(byteBuf)), targetClass);
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize1(Hessian2Input hessian2Input, Class<T> targetClass) {
        try {
            return (T) hessian2Input.readObject(targetClass);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        } finally {
            try {
                hessian2Input.close();
            } catch (IOException e) {
                ExceptionUtils.throwExt(e);
            }
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    @Override
    public int type() {
        return SerializationType.HESSIAN2.getCode();
    }
}
