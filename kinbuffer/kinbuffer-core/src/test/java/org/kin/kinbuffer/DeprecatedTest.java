package org.kin.kinbuffer;

import org.instancio.Instancio;
import org.kin.kinbuffer.io.ByteArrayOutput;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Inputs;
import org.kin.kinbuffer.io.Outputs;
import org.kin.kinbuffer.runtime.Runtime;
import org.kin.kinbuffer.runtime.Schema;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author huangjianqin
 * @date 2023/3/4
 */
public class DeprecatedTest {
    public static void main(String[] args) {
        Schema<DeprecatedMessage> schema = Runtime.getSchema(DeprecatedMessage.class);

        //测试后面版本加上@Deprecated, 看看能不能正常解析到旧版本的bytes
        //frist
        DeprecatedMessage message = Instancio.create(DeprecatedMessage.class);
        ByteArrayOutput output = Outputs.getOutput();
        schema.write(output, message);
        byte[] bytes = output.toByteArray();
        System.out.println(message);
        System.out.println(Arrays.toString(bytes));

        //second
//        byte[] bytes = new byte[]{0, -84, 20, 3, -75, 6, -18, 51, -60, 4, 2, -26, 66, -115, 35, 5, 6, 90, 76, 82,
//                75, 82, 80, -81, 38, 7, 81, 70, 69, 75, 82, 81, 66, -77, 72, 4, 80, 87, 74, 81, -86, 77, 6, 67, 90,
//                83, 74, 65, 86, -39, 54, 5, 74, 75, 76, 72, 85, -59, 6};
//        Input input = Inputs.getInput(bytes);
//        DeprecatedMessage descMessage = schema.newMessage();
//        schema.merge(input, descMessage);
//        System.out.println(descMessage);
    }
}
