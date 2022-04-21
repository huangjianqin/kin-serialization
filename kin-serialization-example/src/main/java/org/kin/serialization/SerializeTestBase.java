package org.kin.serialization;

import com.google.common.base.Stopwatch;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.kin.framework.io.ByteBufferUtils;
import org.kin.framework.utils.ExtensionLoader;
import org.kin.kinbuffer.runtime.Runtime;
import org.kin.serialization.kinbuffer.KinbufferSerialization;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author huangjianqin
 * @date 2020/11/27
 */
public class SerializeTestBase {
    public static Message newMessage() {
        Message message = new Message();

        message.setA(Byte.MIN_VALUE);
        message.setB(Short.MIN_VALUE);
        message.setC(Integer.MIN_VALUE);
        message.setD(Long.MIN_VALUE);
        message.setE(Float.MIN_VALUE);
        message.setF(Double.MIN_VALUE);
        message.setG("Hello Java Bean");
        message.setH(Byte.MAX_VALUE);
        message.setI(Short.MAX_VALUE);
        message.setJ(Integer.MAX_VALUE);
        message.setK(Long.MAX_VALUE);
        message.setL(Float.MAX_VALUE);
        message.setM(Double.MAX_VALUE);
        message.setList(Arrays.asList(1, 2, 3, 4, 5));
        message.setSet(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)));

        Map<Integer, Long> map1 = new HashMap<>();
        map1.put(1, 11L);
        map1.put(2, 22L);
        map1.put(3, 33L);
        map1.put(4, 44L);
        map1.put(5, 55L);
        message.setMap(map1);

        int[] ints = new int[]{1, 2, 3, 4, 5};
        MessageParent[] messageParents = new MessageParent[]{message.clone(), message.clone(), message.clone()};
        List<MessageParent> messageParentList = Arrays.asList(message.clone(), message.clone(), message.clone());
        Set<MessageParent> messageParentSet = new HashSet<>(Arrays.asList(message.clone(), message.clone(), message.clone()));

        Map<Integer, MessageParent> messageParentMap = new HashMap<>();
        messageParentMap.put(1, message.clone());
        messageParentMap.put(2, message.clone());
        messageParentMap.put(3, message.clone());
        messageParentMap.put(4, message.clone());
        messageParentMap.put(5, message.clone());

        int[][] intInts = new int[5][];
        intInts[0] = new int[]{1, 2, 3, 4, 5};
        intInts[1] = new int[]{1, 2, 3, 4, 5};
        intInts[2] = new int[]{1, 2, 3, 4, 5};
        intInts[3] = new int[]{1, 2, 3, 4, 5};
        intInts[4] = new int[]{1, 2, 3, 4, 5};

        MessageParent[][] beanMessageParents = new MessageParent[3][];
        beanMessageParents[0] = new MessageParent[]{message.clone(), message.clone(), message.clone()};
        beanMessageParents[1] = new MessageParent[]{message.clone(), message.clone(), message.clone()};
        beanMessageParents[2] = new MessageParent[]{message.clone(), message.clone(), message.clone()};

        List<List<MessageParent>> listList = new ArrayList<>();
        listList.add(Arrays.asList(message.clone(), message.clone(), message.clone()));
        listList.add(Arrays.asList(message.clone(), message.clone(), message.clone()));
        listList.add(Arrays.asList(message.clone(), message.clone(), message.clone()));

        Set<Set<MessageParent>> setSet = new HashSet<>();
        setSet.add(new HashSet<>(Arrays.asList(message.clone(), message.clone(), message.clone())));
        setSet.add(new HashSet<>(Arrays.asList(message.clone(), message.clone(), message.clone())));
        setSet.add(new HashSet<>(Arrays.asList(message.clone(), message.clone(), message.clone())));

        Map<Integer, Map<Integer, MessageParent>> mapMap = new HashMap<>();
        mapMap.put(1, Collections.singletonMap(11, message.clone()));
        mapMap.put(2, Collections.singletonMap(22, message.clone()));
        mapMap.put(3, Collections.singletonMap(33, message.clone()));
        mapMap.put(4, Collections.singletonMap(44, message.clone()));
        mapMap.put(5, Collections.singletonMap(55, message.clone()));

        List<Map<Integer, MessageParent>> mapList = new ArrayList<>();
        mapList.add(Collections.singletonMap(11, message.clone()));
        mapList.add(Collections.singletonMap(22, message.clone()));
        mapList.add(Collections.singletonMap(33, message.clone()));

        message.setInts(ints);
        message.setMessageParents(messageParents);
        message.setMessageParentList(messageParentList);
        message.setMessageParentSet(messageParentSet);
        message.setMessageParentMap(messageParentMap);
        message.setIntInts(intInts);
        message.setBeanMessageParents(beanMessageParents);
        message.setListList(listList);
        message.setSetSet(setSet);
        message.setMapMap(mapMap);
        message.setMapList(mapList);

        message.setE1(MessageEnum.E);
        message.setE2(MessageEnum.G);

        //object
        message.setO1(1);
        message.setO2("Hello Dynamic Bean");
        message.setO3(messageParents);
        message.setO4(messageParentList);
        message.setO5(messageParentSet);
        message.setO6(messageParentMap);
        message.setO7(intInts);
        message.setO8(beanMessageParents);
        message.setO9(listList);
        message.setO10(setSet);
        message.setO11(mapMap);
        message.setO12(mapList);

        //abstract
        message.setAm1(message.clone());
        message.setAm2(messageParents);
        List<AbstractMessage> am3 = Arrays.asList(message.clone(), message.clone(), message.clone());
        message.setAm3(am3);
        List<List<AbstractMessage>> am4 = new ArrayList<>();
        am4.add(Arrays.asList(message.clone(), message.clone(), message.clone()));
        am4.add(Arrays.asList(message.clone(), message.clone(), message.clone()));
        am4.add(Arrays.asList(message.clone(), message.clone(), message.clone()));
        message.setAm4(am4);
        Map<Integer, AbstractMessage> am5 = new HashMap<>();
        am5.put(1, message.clone());
        am5.put(2, message.clone());
        am5.put(3, message.clone());
        am5.put(4, message.clone());
        message.setAm5(am5);
        Map<Integer, Map<Integer, AbstractMessage>> am6 = new HashMap<>();
        am6.put(1, am5);
        am6.put(2, am5);
        am6.put(3, am5);
        message.setAm6(am6);
        List<Map<Integer, AbstractMessage>> am7 = new ArrayList<>();
        am7.add(am5);
        am7.add(am5);
        message.setAm7(am7);
        return message;
    }

    /**
     * 测试逻辑
     * @param times 序列化和反序列化次数
     */
    private static void test(Serialization serialization, int times) throws IOException, ClassNotFoundException {
        if(serialization instanceof KinbufferSerialization){
            //先初始化Runtime, 排除Runtime static代码块性能耗时
            Runtime.getSchema(Integer.class);
        }

        String t1Result = test1(serialization, times);
        String t2Result = test2(serialization, times);
        String t3Result = test3(serialization, times);

        System.out.println("-------------------结果-------------------");
        System.out.println("byte[] >>> " + t1Result);
        System.out.println("ByteBuffer >>> " + t2Result);
        System.out.println("ByteBuf >>> " + t3Result);
    }

    private static String test1(Serialization serialization, int times) {
        System.out.println("-------------------byte[]-------------------");

        double totalSerializeCostMs = 0;
        double totalDeserializeCostMs = 0;
        int bytesLen = 0;

        for (int i = 0; i < times; i++) {
            Message origin = newMessage();

            Stopwatch watcher = Stopwatch.createStarted();
            byte[] bytes = serialization.serialize(origin);
            watcher.stop();
            totalSerializeCostMs += watcher.elapsed(TimeUnit.MILLISECONDS);

            watcher.reset();
            watcher.start();
            Message deserialize = serialization.deserialize(bytes, Message.class);
            watcher.stop();
            totalDeserializeCostMs += watcher.elapsed(TimeUnit.MILLISECONDS);

            if(i == 0){
                System.out.println(origin);
                System.out.println(deserialize);
                System.out.println(origin.equals(deserialize));
                bytesLen = bytes.length;
            }
        }
        return String.format("序列化字节数:%d, 序列化耗时: %.6fms, 反序列化耗时: %.6fms",
                bytesLen, totalSerializeCostMs / times, totalDeserializeCostMs / times);
    }

    private static String test2(Serialization serialization, int times) {
        System.out.println("-------------------ByteBuffer-------------------");

        double totalSerializeCostMs = 0;
        double totalDeserializeCostMs = 0;
        int bytesLen = 0;
        int capacity = 0;

        for (int i = 0; i < times; i++) {
            Message origin = newMessage();
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(64);

            Stopwatch watcher = Stopwatch.createStarted();
            byteBuffer = serialization.serialize(byteBuffer, origin);
            watcher.stop();
            totalSerializeCostMs += watcher.elapsed(TimeUnit.MILLISECONDS);

            watcher.reset();
            watcher.start();
            ByteBufferUtils.toReadMode(byteBuffer);

            int len = ByteBufferUtils.getReadableBytes(byteBuffer);
            Message deserialize = serialization.deserialize(byteBuffer, Message.class);
            watcher.stop();
            totalDeserializeCostMs += watcher.elapsed(TimeUnit.MILLISECONDS);

            if(i == 0){
//                System.out.println(origin);
//                System.out.println(deserialize);
                System.out.println(origin.equals(deserialize));
                bytesLen = len;
                capacity = byteBuffer.capacity();
            }
        }
        return String.format("序列化字节数:%d, 序列化耗时: %.6fms, 反序列化耗时: %.6fms, buffer capacity:%d",
                bytesLen, totalSerializeCostMs / times, totalDeserializeCostMs / times, capacity);
    }

    private static String test3(Serialization serialization, int times) {
        System.out.println("-------------------ByteBuf-------------------");

        double totalSerializeCostMs = 0;
        double totalDeserializeCostMs = 0;
        int bytesLen = 0;
        int capacity = 0;

        for (int i = 0; i < times; i++) {
            Message origin = newMessage();
            ByteBuf buffer = Unpooled.directBuffer();

            Stopwatch watcher = Stopwatch.createStarted();
            serialization.serialize(buffer, origin);
            watcher.stop();
            totalSerializeCostMs += watcher.elapsed(TimeUnit.MILLISECONDS);
            int len = buffer.readableBytes();

            watcher.reset();
            watcher.start();
            Message deserialize = serialization.deserialize(buffer, Message.class);
            watcher.stop();
            totalDeserializeCostMs += watcher.elapsed(TimeUnit.MILLISECONDS);

            if(i == 0){
//                System.out.println(origin);
//                System.out.println(deserialize);
                System.out.println(origin.equals(deserialize));
                bytesLen = len;
                capacity = buffer.capacity();
            }

            buffer.release();
        }

        return String.format("序列化字节数:%d, 序列化耗时: %.6fms, 反序列化耗时: %.6fms, buffer capacity:%d",
                bytesLen, totalSerializeCostMs / times, totalDeserializeCostMs / times, capacity);
    }

    //-----------------------------builder-------------------------------------
    public static Builder builder(SerializationType type) {
        return new Builder(type);
    }

    public static Builder builder(Serialization serialization) {
        return new Builder(serialization);
    }

    public static class Builder {
        final Serialization serialization;

        public Builder(SerializationType type) {
            this(ExtensionLoader.getExtension(Serialization.class, type.getCode()));
        }

        public Builder(Serialization serialization) {
            this.serialization = serialization;
        }

        public void run(){
            run(1);
        }

        public void run(int times) {
            try {
                test(serialization, times);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
