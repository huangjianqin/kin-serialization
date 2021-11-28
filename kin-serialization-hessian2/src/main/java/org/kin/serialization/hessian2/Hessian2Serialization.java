package org.kin.serialization.hessian2;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.kin.framework.io.ByteBufferInputStream;
import org.kin.framework.io.ByteBufferOutputStream;
import org.kin.framework.io.ExpandableByteBufferOutputStream;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.serialization.OutputStreams;
import org.kin.serialization.Serialization;
import org.kin.serialization.SerializationType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created by 健勤 on 2017/2/9.
 */
@Extension(value = "hessian2", code = 3)
public class Hessian2Serialization implements Serialization {
    @Override
    public byte[] serialize(Object target) {
        // TODO: 2021/11/28  统一加上类型检查
        if (target == null) {
            throw new NullPointerException("Serialized object must be not null");
        }

        if (!(target instanceof Serializable)) {
            throw new IllegalStateException("Serialized class " + target.getClass().getSimpleName() + " must implement java.io.Serializable");
        }

        ByteArrayOutputStream baos = OutputStreams.getByteArrayOutputStream();
        try {
            serialize0(new Hessian2Output(baos), target);
            return baos.toByteArray();
        }finally {
            OutputStreams.resetBuf(baos);
        }
    }

    @Override
    public <T> ByteBuffer serialize(ByteBuffer byteBuffer, T target) {
        ExpandableByteBufferOutputStream bbos = new ExpandableByteBufferOutputStream(byteBuffer);
        serialize0(new Hessian2Output(bbos), target);
        return bbos.getSink();
    }

    @Override
    public <T> void serialize(ByteBuf byteBuf, T target) {
        serialize0(new Hessian2Output(new ByteBufOutputStream(byteBuf)), target);
    }

    private <T> void serialize0(Hessian2Output output, T target){
        try{
            output.writeObject(target);
            output.flush();
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        finally {
            try {
                output.close();
            } catch (IOException e) {
                ExceptionUtils.throwExt(e);
            }
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> targetClass) {
        if (bytes == null || bytes.length <= 0) {
            throw new IllegalStateException("byte array must be not null or it's length must be greater than zero");
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return deserialize0(new Hessian2Input(inputStream), targetClass);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    @Override
    public <T> T deserialize(ByteBuffer byteBuffer, Class<T> targetClass) {
        return deserialize0(new Hessian2Input(new ByteBufferInputStream(byteBuffer)), targetClass);
    }

    @Override
    public <T> T deserialize(ByteBuf byteBuf, Class<T> targetClass) {
        return deserialize0(new Hessian2Input(new ByteBufInputStream(byteBuf)), targetClass);
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize0(Hessian2Input hessian2Input, Class<T> targetClass){
        try{
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
