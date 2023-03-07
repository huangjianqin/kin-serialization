package org.kin.kinbuffer.io;

/**
 * @author huangjianqin
 * @date 2023/3/8
 */
public class IOTest {
    public static void main(String[] args) {
        System.out.println(Integer.MAX_VALUE);
        System.out.println(Integer.MAX_VALUE / 2);
        System.out.println(Integer.MIN_VALUE / 2);
        System.out.println(Long.MAX_VALUE / 2);
        System.out.println(Long.MIN_VALUE / 2);
        System.out.println("----------------------");
        ByteArrayOutput output = Outputs.getOutput();
        output.writeInt32(123);
        output.writeUInt32(Integer.MAX_VALUE + 10);
        output.writeSInt32(Integer.MIN_VALUE / 2);
        output.writeVarInt32(Integer.MAX_VALUE / 2);
        output.writeInt64(Long.MAX_VALUE / 2);
        output.writeSInt64(Long.MIN_VALUE / 2);
        output.writeVarInt64(Long.MAX_VALUE / 2);
        output.writeSVarInt64(Long.MIN_VALUE / 2);

        byte[] bytes = output.toByteArray();
        Input input = Inputs.getInput(bytes);
        System.out.println(input.readInt32());
        System.out.println(input.readUInt32());
        System.out.println(input.readSInt32());
        System.out.println(input.readVarInt32());
        System.out.println(input.readInt64());
        System.out.println(input.readSInt64());
        System.out.println(input.readVarInt64());
        System.out.println(input.readSVarInt64());
    }
}
