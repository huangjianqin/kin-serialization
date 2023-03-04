package org.kin.serialization.jsonb;

import com.alibaba.fastjson2.JSONB;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.kin.framework.io.ByteBufferInputStream;
import org.kin.framework.io.ScalableByteBufferOutputStream;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.serialization.AbstractSerialization;
import org.kin.serialization.SerializationType;

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
    protected <T> T deserialize0(byte[] bytes, Class<T> targetClass) {
        return JSONB.parseObject(bytes, targetClass);
    }

    @Override
    public int type() {
        return SerializationType.JSONB.getCode();
    }
}
