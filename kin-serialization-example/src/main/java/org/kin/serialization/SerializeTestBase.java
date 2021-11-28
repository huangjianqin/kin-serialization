package org.kin.serialization;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.kin.framework.io.ByteBufferUtils;
import org.kin.framework.utils.ExtensionLoader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author huangjianqin
 * @date 2020/11/27
 */
public class SerializeTestBase {
    public static final ExtensionLoader LOADER = ExtensionLoader.load();

    private static Message newMessage(){
        return new Message();
    }

    /**
     * 测试逻辑
     */
    private static void test(Serialization serialization) throws IOException, ClassNotFoundException {
        boolean t1Result = test1(serialization);
        boolean t2Result = test2(serialization);
        boolean t3Result = test3(serialization);

        System.out.println("-------------------结果-------------------");
        System.out.println("byte[] >>> " + t1Result);
        System.out.println("ByteBuffer >>> " + t2Result);
        System.out.println("ByteBuf >>> " + t3Result);
    }

    private static boolean test1(Serialization serialization){
        System.out.println("-------------------byte[]-------------------");
        Message origin = newMessage();

        byte[] bytes = serialization.serialize(origin);

        Message deserialize = serialization.deserialize(bytes, Message.class);

        System.out.println(origin);
        System.out.println(deserialize);
        return origin.equals(deserialize);
    }

    private static boolean test2(Serialization serialization){
        System.out.println("-------------------ByteBuffer-------------------");
        Message origin = newMessage();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(64);

        byteBuffer = serialization.serialize(byteBuffer, origin);

        ByteBufferUtils.toReadMode(byteBuffer);
        Message deserialize = serialization.deserialize(byteBuffer, Message.class);

        System.out.println(origin);
        System.out.println(deserialize);
        return origin.equals(deserialize);
    }

    private static boolean test3(Serialization serialization){
        System.out.println("-------------------ByteBuf-------------------");
        Message origin = newMessage();
        ByteBuf buffer = Unpooled.directBuffer();

        serialization.serialize(buffer, origin);

        Message deserialize = serialization.deserialize(buffer, Message.class);

        System.out.println(origin);
        System.out.println(deserialize);
        return origin.equals(deserialize);
    }

    //-----------------------------builder-------------------------------------
    public static Builder builder(SerializationType type) {
        return new Builder(type);
    }

    public static Builder builder(Serialization serialization) {
        return new Builder(serialization);
    }

    public static class Builder {
        private final Serialization serialization;

        public Builder(SerializationType type) {
            this(LOADER.getExtension(Serialization.class, type.getCode()));
        }

        public Builder(Serialization serialization) {
            this.serialization = serialization;
        }


        public void run() {
            try {
                test(serialization);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
