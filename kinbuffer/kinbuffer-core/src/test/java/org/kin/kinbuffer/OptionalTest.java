package org.kin.kinbuffer;

import org.instancio.Instancio;
import org.kin.kinbuffer.io.ByteArrayOutput;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Inputs;
import org.kin.kinbuffer.io.Outputs;
import org.kin.kinbuffer.runtime.Runtime;
import org.kin.kinbuffer.runtime.Schema;

import java.util.concurrent.TimeUnit;

/**
 * @author huangjianqin
 * @date 2023/3/4
 */
public class OptionalTest {
    public static void main(String[] args) {
        OptionalMessage message = Instancio.create(OptionalMessage.class);
//        模拟null
        message.setD(null);
        //模拟没有@Optional注解的字段为null
//        message.setB(null);
        Schema<OptionalMessage> schema = Runtime.getSchema(OptionalMessage.class);
        ByteArrayOutput output = Outputs.getOutput();

        schema.write(output, message);

        byte[] bytes = output.toByteArray();
        Input input = Inputs.getInput(bytes);
        OptionalMessage descMessage = schema.newMessage();
        schema.merge(input, descMessage);

        System.out.println(message);
        System.out.println(descMessage);
        System.out.println(message.equals(descMessage));
        System.out.println(bytes.length);

    }
}
