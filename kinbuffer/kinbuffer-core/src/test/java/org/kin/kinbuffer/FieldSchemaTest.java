package org.kin.kinbuffer;

import com.google.common.base.Stopwatch;
import org.kin.kinbuffer.io.ByteArrayOutput;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Inputs;
import org.kin.kinbuffer.io.Outputs;
import org.kin.kinbuffer.runtime.Runtime;
import org.kin.kinbuffer.runtime.Schema;

import java.util.concurrent.TimeUnit;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
public class FieldSchemaTest {
    public static void main(String[] args){
        Message message = Message.instance();

        Stopwatch watcher = Stopwatch.createStarted();
        Runtime.registerClass(Message.class);
        Runtime.registerClass(MessageParent.class);
        Schema<Message> schema = Runtime.getSchema(Message.class);
        watcher.stop();
        long schemaCostMs = watcher.elapsed(TimeUnit.MILLISECONDS);

        ByteArrayOutput output = Outputs.getOutput();

        watcher.reset();
        watcher.start();
        schema.write(output, message);
        watcher.stop();
        long writeCostMs = watcher.elapsed(TimeUnit.MILLISECONDS);

        byte[] bytes = output.toByteArray();
        Input input = Inputs.getInput(bytes);
        Message descMessage = schema.newMessage();

        watcher.reset();
        watcher.start();
        schema.merge(input, descMessage);
        watcher.stop();
        long readCostMs = watcher.elapsed(TimeUnit.MILLISECONDS);

        System.out.println(message);
        System.out.println("--------------------------------------------------------------------------------------------------------------");
        System.out.println(descMessage);
        System.out.println("--------------------------------------------------------------------------------------------------------------");
        System.out.println(String.format("序列化字节数:%d, schema耗时: %dms, 读耗时: %dms, 写耗时: %dms", bytes.length, schemaCostMs, readCostMs, writeCostMs));
        System.out.println(message.equals(descMessage));
    }
}
