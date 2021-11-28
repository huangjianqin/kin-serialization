package org.kin.serialization.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.serialization.Serialization;

import java.nio.ByteBuffer;

/**
 * @author huangjianqin
 * @date 2020/11/29
 */
@Extension(value = "protobufJson", code = -1)
public class ProtobufJsonSerialization implements Serialization {
    @Override
    public byte[] serialize(Object target) {
        return Protobufs.serializeJson(target).getBytes();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> targetClass) {
        return Protobufs.deserializeJson(new String(bytes), targetClass);
    }

    @Override
    public int type() {
        //未用
        return -1;
    }


}