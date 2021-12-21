package org.kin.kinbuffer;

import com.google.common.base.Stopwatch;
import org.kin.framework.io.StreamInput;
import org.kin.framework.io.StreamOutput;
import org.kin.kinbuffer.io.DefaultInput;
import org.kin.kinbuffer.io.DefaultOutput;
import org.kin.kinbuffer.schema.Runtime;
import org.kin.kinbuffer.schema.Schema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author huangjianqin
 * @date 2021/12/18
 */
public class RuntimeSchemaTest {
    public static void main(String[] args) throws IOException {
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

        message.setE1(MessageEnum.E);
        message.setE2(MessageEnum.G);

        Stopwatch watcher = Stopwatch.createStarted();

        Schema<Message> schema = Runtime.getSchema(Message.class);
//        watcher.stop();
        long schemaCostMs = watcher.elapsed(TimeUnit.MILLISECONDS);

        // TODO: 2021/12/19 优化对外api接口
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DefaultOutput output = new DefaultOutput(new StreamOutput(baos));

        watcher.reset();
        watcher.start();
        schema.write(output, message);
        watcher.stop();
        long writeCostMs = watcher.elapsed(TimeUnit.MILLISECONDS);

        baos.close();
        byte[] bytes = baos.toByteArray();
        DefaultInput input = new DefaultInput(new StreamInput(new ByteArrayInputStream(bytes)));
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
