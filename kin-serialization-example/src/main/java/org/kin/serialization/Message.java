package org.kin.serialization;

import java.util.*;

/**
 * @author huangjianqin
 * @date 2021/9/9
 */
public class Message extends MessageParent {
    private static final long serialVersionUID = -3700060157525404774L;
    private int[] ints = new int[]{1, 2, 3, 4, 5};
    private MessageParent[] messageParents = new MessageParent[]{new MessageParent(), new MessageParent(), new MessageParent()};
    private List<MessageParent> messageParentList = Arrays.asList(new MessageParent(), new MessageParent(), new MessageParent());
    private Set<MessageParent> messageParentSet = new HashSet<>(Arrays.asList(new MessageParent(), new MessageParent(), new MessageParent()));
    private Map<Integer, MessageParent> beanMap = new HashMap<>();

    {
        beanMap.put(1, new MessageParent());
        beanMap.put(2, new MessageParent());
        beanMap.put(3, new MessageParent());
        beanMap.put(4, new MessageParent());
        beanMap.put(5, new MessageParent());
    }

    private int[][] intInts = new int[5][];

    {
        intInts[0] = new int[]{1, 2, 3, 4, 5};
        intInts[1] = new int[]{1, 2, 3, 4, 5};
        intInts[2] = new int[]{1, 2, 3, 4, 5};
        intInts[3] = new int[]{1, 2, 3, 4, 5};
        intInts[4] = new int[]{1, 2, 3, 4, 5};
    }

    private MessageParent[][] beanMessageParents = new MessageParent[3][];

    {
        beanMessageParents[0] = new MessageParent[]{new MessageParent(), new MessageParent(), new MessageParent()};
        beanMessageParents[1] = new MessageParent[]{new MessageParent(), new MessageParent(), new MessageParent()};
        beanMessageParents[2] = new MessageParent[]{new MessageParent(), new MessageParent(), new MessageParent()};
    }

    private List<List<MessageParent>> listList = new ArrayList<>();

    {
        listList.add(Arrays.asList(new MessageParent(), new MessageParent(), new MessageParent()));
        listList.add(Arrays.asList(new MessageParent(), new MessageParent(), new MessageParent()));
        listList.add(Arrays.asList(new MessageParent(), new MessageParent(), new MessageParent()));
    }

    private Set<Set<MessageParent>> setSet = new HashSet<>();

    {
        setSet.add(new HashSet<>(Arrays.asList(new MessageParent(), new MessageParent(), new MessageParent())));
        setSet.add(new HashSet<>(Arrays.asList(new MessageParent(), new MessageParent(), new MessageParent())));
        setSet.add(new HashSet<>(Arrays.asList(new MessageParent(), new MessageParent(), new MessageParent())));
    }

    private Map<Integer, Map<Integer, MessageParent>> mapMap = new HashMap<>();

    {
        mapMap.put(1, Collections.singletonMap(11, new MessageParent()));
        mapMap.put(2, Collections.singletonMap(22, new MessageParent()));
        mapMap.put(3, Collections.singletonMap(33, new MessageParent()));
        mapMap.put(4, Collections.singletonMap(44, new MessageParent()));
        mapMap.put(5, Collections.singletonMap(55, new MessageParent()));
    }

    private List<Map<Integer, MessageParent>> mapList = new ArrayList<>();

    {
        mapList.add(Collections.singletonMap(11, new MessageParent()));
        mapList.add(Collections.singletonMap(22, new MessageParent()));
        mapList.add(Collections.singletonMap(33, new MessageParent()));
    }

    //setter && getter


    public int[] getInts() {
        return ints;
    }

    public void setInts(int[] ints) {
        this.ints = ints;
    }

    public MessageParent[] getSourceParents() {
        return messageParents;
    }

    public void setSourceParents(MessageParent[] messageParents) {
        this.messageParents = messageParents;
    }

    public List<MessageParent> getSourceParentList() {
        return messageParentList;
    }

    public void setSourceParentList(List<MessageParent> messageParentList) {
        this.messageParentList = messageParentList;
    }

    public Set<MessageParent> getSourceParentSet() {
        return messageParentSet;
    }

    public void setSourceParentSet(Set<MessageParent> messageParentSet) {
        this.messageParentSet = messageParentSet;
    }

    public Map<Integer, MessageParent> getBeanMap() {
        return beanMap;
    }

    public void setBeanMap(Map<Integer, MessageParent> beanMap) {
        this.beanMap = beanMap;
    }

    public int[][] getIntInts() {
        return intInts;
    }

    public void setIntInts(int[][] intInts) {
        this.intInts = intInts;
    }

    public MessageParent[][] getBeanSourceParents() {
        return beanMessageParents;
    }

    public void setBeanSourceParents(MessageParent[][] beanMessageParents) {
        this.beanMessageParents = beanMessageParents;
    }

    public List<List<MessageParent>> getListList() {
        return listList;
    }

    public void setListList(List<List<MessageParent>> listList) {
        this.listList = listList;
    }

    public Set<Set<MessageParent>> getSetSet() {
        return setSet;
    }

    public void setSetSet(Set<Set<MessageParent>> setSet) {
        this.setSet = setSet;
    }

    public Map<Integer, Map<Integer, MessageParent>> getMapMap() {
        return mapMap;
    }

    public void setMapMap(Map<Integer, Map<Integer, MessageParent>> mapMap) {
        this.mapMap = mapMap;
    }

    public List<Map<Integer, MessageParent>> getMapList() {
        return mapList;
    }

    public void setMapList(List<Map<Integer, MessageParent>> mapList) {
        this.mapList = mapList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Message)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Message source = (Message) o;

        return Arrays.equals(ints, source.ints) &&
                Arrays.equals(messageParents, source.messageParents) &&
                Objects.equals(messageParentList, source.messageParentList) &&
                Objects.equals(messageParentSet, source.messageParentSet) &&
                Objects.equals(beanMap, source.beanMap) &&
                Arrays.deepEquals(intInts, source.intInts) &&
                Arrays.deepEquals(beanMessageParents, source.beanMessageParents) &&
                Objects.equals(listList, source.listList) &&
                Objects.equals(setSet, source.setSet) &&
                Objects.equals(mapMap, source.mapMap) &&
                Objects.equals(mapList, source.mapList);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), messageParentList, messageParentSet, beanMap, listList, setSet, mapMap, mapList);
        result = 31 * result + Arrays.hashCode(ints);
        result = 31 * result + Arrays.hashCode(messageParents);
        result = 31 * result + Arrays.hashCode(intInts);
        result = 31 * result + Arrays.hashCode(beanMessageParents);
        return result;
    }

    @Override
    public String toString() {
        return "Source{" +
                "a=" + a +
                ", b=" + b +
                ", c=" + c +
                ", d=" + d +
                ", e=" + e +
                ", f=" + f +
                ", g='" + g + '\'' +
                ", h=" + h +
                ", i=" + i +
                ", j=" + j +
                ", k=" + k +
                ", l=" + l +
                ", m=" + m +
                ", list=" + list +
                ", set=" + set +
                ", map=" + map +
                ", ints=" + Arrays.toString(ints) +
                ", beans=" + Arrays.toString(messageParents) +
                ", beanList=" + messageParentList +
                ", beanSet=" + messageParentSet +
                ", beanMap=" + beanMap +
                ", intInts=" + Arrays.toString(intInts) +
                ", beanBeans=" + Arrays.toString(beanMessageParents) +
                ", listList=" + listList +
                ", setSet=" + setSet +
                ", mapMap=" + mapMap +
                ", mapList=" + mapList +
                "} " + super.toString();
    }
}
