package org.kin.serialization.hessian2;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
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
import java.nio.ByteBuffer;

/**
 * Created by 健勤 on 2017/2/9.
 */
@Extension(value = "hessian2", code = 3)
public final class Hessian2Serialization extends AbstractSerialization {
    @Override
    protected byte[] serialize0(Object target) {
        ByteArrayOutputStream baos = OutputStreams.getByteArrayOutputStream();
        try {
            serialize1(new Hessian2Output(baos), target);
            return baos.toByteArray();
        } finally {
            OutputStreams.resetBuf(baos);
        }
    }

    @Override
    protected <T> ByteBuffer serialize0(ByteBuffer byteBuffer, T target) {
        ScalableByteBufferOutputStream ebbos = new ScalableByteBufferOutputStream(byteBuffer);
        serialize1(new Hessian2Output(ebbos), target);
        return ebbos.getSink();
    }

    @Override
    protected <T> void serialize0(ByteBuf byteBuf, T target) {
        serialize1(new Hessian2Output(new ByteBufOutputStream(byteBuf)), target);
    }

    private <T> void serialize1(Hessian2Output output, T target) {
        try {
            output.writeObject(target);
            output.flush();
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                ExceptionUtils.throwExt(e);
            }
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
