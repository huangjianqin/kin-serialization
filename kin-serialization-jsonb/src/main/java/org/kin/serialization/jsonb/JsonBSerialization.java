package org.kin.serialization.jsonb;

import com.alibaba.fastjson2.JSONB;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.kin.framework.io.ByteBufferInputStream;
import org.kin.framework.io.ByteBufferUtils;
import org.kin.framework.io.ScalableByteBufferOutputStream;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.serialization.AbstractSerialization;
import org.kin.serialization.SerializationType;
import org.kin.transport.netty.utils.ByteBufUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author huangjianqin
 * @date 2023/3/4
 */
@Extension(value = "jsonb", code = 9)
public class JsonBSerialization extends AbstractSerialization {
    @Override
    protected byte[] serialize0(Object target) {
       return JSONB.toBytes(target);
    }

    @Override
    protected byte[] serializeMany(Object[] objects) {
        return serialize0(objects);
    }

    @Override
    protected ByteBuffer serializeMany(ByteBuffer byteBuffer, Object[] objects) {
        byte[] bytes = serializeMany(objects);
        ByteBuffer ret = ByteBufferUtils.ensureWritableBytes(byteBuffer, bytes.length);
        ret.put(bytes);
        return ret;
    }

    @Override
    protected void serializeMany(ByteBuf byteBuf, Object[] objects) {
        byte[] bytes = serializeMany(objects);
        byteBuf.writeBytes(bytes);
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> targetClass) {
        return JSONB.parseObject(bytes, targetClass);
    }

    @Override
    protected Object[] deserializeMany(byte[] bytes, Class<?>... targetClasses) {
        return JSONB.parseArray(bytes, targetClasses).toArray();
    }

    @Override
    protected Object[] deserializeMany(ByteBuffer byteBuffer, Class<?>... targetClasses) {
        return JSONB.parseArray(ByteBufferUtils.toBytes(byteBuffer), targetClasses).toArray();
    }

    @Override
    protected Object[] deserializeMany(ByteBuf byteBuf, Class<?>... targetClasses) {
        return JSONB.parseArray(ByteBufUtils.toBytes(byteBuf), targetClasses).toArray();
    }

    @Override
    public int type() {
        return SerializationType.JSONB.getCode();
    }
}
