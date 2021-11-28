package org.kin.serialization.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.serialization.AbstractSerialization;
import org.kin.serialization.Serialization;

import java.nio.ByteBuffer;

/**
 * @author huangjianqin
 * @date 2020/11/29
 */
@Extension(value = "protobufJson", code = -1)
public final class ProtobufJsonSerialization extends AbstractSerialization {
    @Override
    protected byte[] serialize0(Object target) {
        return Protobufs.serializeJson(target).getBytes();
    }

    @Override
    protected  <T> T deserialize0(byte[] bytes, Class<T> targetClass) {
        return Protobufs.deserializeJson(new String(bytes), targetClass);
    }

    @Override
    public int type() {
        //未用
        return -1;
    }


}