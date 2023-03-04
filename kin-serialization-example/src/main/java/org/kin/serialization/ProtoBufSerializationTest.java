package org.kin.serialization;

import com.google.protobuf.Any;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.kin.framework.io.ByteBufferUtils;
import org.kin.serialization.protobuf.ProtoBufMessage;
import org.kin.serialization.protobuf.ProtobufSerialization;
import org.kin.serialization.protobuf.Protobufs;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author huangjianqin
 * @date 2020/11/29
 */
public class ProtoBufSerializationTest {
    public static void main(String[] args) throws IOException {
        Protobufs.scanAndRegister("org.kin.serialization");

        ProtobufSerialization serialization = new ProtobufSerialization();
        ProtoBufMessage copy = ProtoBufMessage.newBuilder().setA(2).setB("empty").build();
        ProtoBufMessage origin = ProtoBufMessage.newBuilder().setA(1).setB("aa").setData(Any.pack(copy)).build();


        System.out.println("-------------------------------------------------origin--------------------------------------------------------------");
        System.out.println(origin);

        System.out.println("-------------------------------------------------bytes---------------------------------------------------------------");
        byte[] bytes = serialization.serialize(origin);
        System.out.println(bytes.length);
        System.out.println(serialization.deserialize(bytes, ProtoBufMessage.class));

        System.out.println("-------------------------------------------------ByteBuffer----------------------------------------------------------");
        ByteBuffer byteBuffer = serialization.serialize(ByteBuffer.allocateDirect(64), origin);
        ByteBufferUtils.toReadMode(byteBuffer);
        System.out.println(ByteBufferUtils.getReadableBytes(byteBuffer));
        System.out.println(serialization.deserialize(byteBuffer, ProtoBufMessage.class));

        System.out.println("-------------------------------------------------ByteBuf-------------------------------------------------------------");
        ByteBuf byteBuf = Unpooled.buffer();
        serialization.serialize(byteBuf, origin);
        System.out.println(byteBuf.readableBytes());
        System.out.println(serialization.deserialize(byteBuf, ProtoBufMessage.class));
    }
}
