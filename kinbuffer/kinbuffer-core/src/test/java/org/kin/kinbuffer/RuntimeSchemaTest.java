package org.kin.kinbuffer;

import com.google.common.base.Stopwatch;
import org.kin.kinbuffer.io.DefaultInput;
import org.kin.kinbuffer.io.DefaultOutput;
import org.kin.kinbuffer.runtime.Runtime;
import org.kin.kinbuffer.runtime.Schema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
public class RuntimeSchemaTest {
    public static void main(String[] args) throws IOException {
        Message message = Message.instance();

        Stopwatch watcher = Stopwatch.createStarted();
        Schema<Message> schema = Runtime.getSchema(Message.class);
        watcher.stop();
        long schemaCostMs = watcher.elapsed(TimeUnit.MILLISECONDS);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DefaultOutput output = DefaultOutput.stream(baos);

        watcher.reset();
        watcher.start();
        schema.write(output, message);
        watcher.stop();
        long writeCostMs = watcher.elapsed(TimeUnit.MILLISECONDS);

        baos.close();
        byte[] bytes = baos.toByteArray();
        DefaultInput input = DefaultInput.stream(new ByteArrayInputStream(bytes));
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
