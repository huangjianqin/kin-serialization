package org.kin.kinbuffer.io;

import org.kin.framework.io.ByteBufferUtils;
import org.kin.framework.io.ScalableByteArray;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author huangjianqin
 * @date 2022/4/16
 */
public class ByteArrayOutputTest {
    public static void main(String[] args) {
        String str = "java.util.Collections$SingletonMap";

        ByteArrayOutput output = new ByteArrayOutput(new ScalableByteArray(8));
        output.writeBoolean(true);
        output.writeByte(2);
        output.writeFloat(1.1f);
        output.writeDouble(1.1d);
        output.writeInt64(1000l);
        output.writeInt32(1);
        output.writeInt32(200);
        output.writeString(str);
        output.writeInt32(200);
        output.writeInt32(10000);
        output.writeSInt32(-100000);
        //放在最后
        output.writeBytes(str.getBytes(StandardCharsets.UTF_8));

        byte[] bytes = output.toByteArray();
        System.out.println(Arrays.toString(bytes));

        ByteArrayInput input = new ByteArrayInput(bytes);
        System.out.println(input.readBoolean());
        System.out.println(input.readByte());
        System.out.println(input.readFloat());
        System.out.println(input.readDouble());
        System.out.println(input.readInt64());
        System.out.println(input.readInt32());
        System.out.println(input.readInt32());
        System.out.println(input.readString());
        System.out.println(input.readInt32());
        System.out.println(input.readInt32());
        System.out.println(input.readSInt32());
        //放在最后
        System.out.println(Arrays.toString(input.readBytes()));
    }
}
