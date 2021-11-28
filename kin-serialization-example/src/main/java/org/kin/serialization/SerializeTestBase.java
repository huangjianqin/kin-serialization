package org.kin.serialization;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.kin.framework.io.ByteBufferUtils;
import org.kin.framework.utils.ExtensionLoader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * @author huangjianqin
 * @date 2020/11/27
 */
public class SerializeTestBase {
    public static final ExtensionLoader LOADER = ExtensionLoader.load();

    static Message newMessage() {
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

        Map<Integer, MessageParent> beanMap = new HashMap<>();
        beanMap.put(1, message.clone());
        beanMap.put(2, message.clone());
        beanMap.put(3, message.clone());
        beanMap.put(4, message.clone());
        beanMap.put(5, message.clone());

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
        message.setBeanMap(beanMap);
        message.setIntInts(intInts);
        message.setBeanMessageParents(beanMessageParents);
        message.setListList(listList);
        message.setSetSet(setSet);
        message.setMapMap(mapMap);
        message.setMapList(mapList);
        return message;
    }

    /**
     * 测试逻辑
     */
    static void test(Serialization serialization) throws IOException, ClassNotFoundException {
        boolean t1Result = test1(serialization);
        boolean t2Result = test2(serialization);
        boolean t3Result = test3(serialization);

        System.out.println("-------------------结果-------------------");
        System.out.println("byte[] >>> " + t1Result);
        System.out.println("ByteBuffer >>> " + t2Result);
        System.out.println("ByteBuf >>> " + t3Result);
    }

    static boolean test1(Serialization serialization) {
        System.out.println("-------------------byte[]-------------------");
        Message origin = newMessage();

        byte[] bytes = serialization.serialize(origin);

        Message deserialize = serialization.deserialize(bytes, Message.class);

        System.out.println(origin);
        System.out.println(deserialize);
        return origin.equals(deserialize);
    }

    static boolean test2(Serialization serialization) {
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

    static boolean test3(Serialization serialization) {
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
        final Serialization serialization;

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
