package org.kin.kinbuffer;

import org.instancio.Instancio;
import org.kin.kinbuffer.io.ByteArrayOutput;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Inputs;
import org.kin.kinbuffer.io.Outputs;
import org.kin.kinbuffer.runtime.Runtime;
import org.kin.kinbuffer.runtime.Schema;

/**
 * @author huangjianqin
 * @date 2023/3/5
 */
public class GenericMessageTest {
    public static void main(String[] args) {
        GenericMessage message = new GenericMessage();
        message.setGp(Instancio.create(GenericImpl1.class));
        message.setGp1(Instancio.create(GenericImpl2.class));
        Schema<GenericMessage> schema = Runtime.getSchema(GenericMessage.class);
        ByteArrayOutput output = Outputs.getOutput();

        schema.write(output, message);

        byte[] bytes = output.toByteArray();
        Input input = Inputs.getInput(bytes);
        GenericMessage descMessage = schema.newMessage();
        schema.merge(input, descMessage);

        System.out.println(message);
        System.out.println(descMessage);
        System.out.println(message.equals(descMessage));
        System.out.println(bytes.length);
    }
}
