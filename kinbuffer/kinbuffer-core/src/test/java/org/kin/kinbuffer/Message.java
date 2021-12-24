package org.kin.kinbuffer;

import java.util.*;

/**
 * @author huangjianqin
 * @date 2021/9/9
 */
public class Message extends MessageParent {
    private static final long serialVersionUID = -3700060157525404774L;

    private int[] ints;
    private MessageParent[] messageParents;
    private List<MessageParent> messageParentList = new ArrayList<>();
    private Set<MessageParent> messageParentSet = new HashSet<>();
    private Map<Integer, MessageParent> beanMap = new HashMap<>();
    private int[][] intInts;
    private MessageParent[][] beanMessageParents;
    private List<List<MessageParent>> listList = new ArrayList<>();
    private Set<Set<MessageParent>> setSet = new HashSet<>();
    private Map<Integer, Map<Integer, MessageParent>> mapMap = new HashMap<>();
    private List<Map<Integer, MessageParent>> mapList = new ArrayList<>();
    private MessageEnum e1;
    private MessageEnum e2;
    private Object o1;
    private Object o2;
    private Object o3;
    private Object o4;
    private Object o5;
    private Object o6;
    private Object o7;
    private Object o8;
    private Object o9;
    private Object o10;
    private Object o11;
    private Object o12;

    //setter && getter
    public int[] getInts() {
        return ints;
    }

    public void setInts(int[] ints) {
        this.ints = ints;
    }

    public MessageParent[] getMessageParents() {
        return messageParents;
    }

    public Message setMessageParents(MessageParent[] messageParents) {
        this.messageParents = messageParents;
        return this;
    }

    public List<MessageParent> getMessageParentList() {
        return messageParentList;
    }

    public Message setMessageParentList(List<MessageParent> messageParentList) {
        this.messageParentList = messageParentList;
        return this;
    }

    public Set<MessageParent> getMessageParentSet() {
        return messageParentSet;
    }

    public Message setMessageParentSet(Set<MessageParent> messageParentSet) {
        this.messageParentSet = messageParentSet;
        return this;
    }

    public Map<Integer, MessageParent> getBeanMap() {
        return beanMap;
    }

    public Message setBeanMap(Map<Integer, MessageParent> beanMap) {
        this.beanMap = beanMap;
        return this;
    }

    public int[][] getIntInts() {
        return intInts;
    }

    public Message setIntInts(int[][] intInts) {
        this.intInts = intInts;
        return this;
    }

    public MessageParent[][] getBeanMessageParents() {
        return beanMessageParents;
    }

    public Message setBeanMessageParents(MessageParent[][] beanMessageParents) {
        this.beanMessageParents = beanMessageParents;
        return this;
    }

    public List<List<MessageParent>> getListList() {
        return listList;
    }

    public Message setListList(List<List<MessageParent>> listList) {
        this.listList = listList;
        return this;
    }

    public Set<Set<MessageParent>> getSetSet() {
        return setSet;
    }

    public Message setSetSet(Set<Set<MessageParent>> setSet) {
        this.setSet = setSet;
        return this;
    }

    public Map<Integer, Map<Integer, MessageParent>> getMapMap() {
        return mapMap;
    }

    public Message setMapMap(Map<Integer, Map<Integer, MessageParent>> mapMap) {
        this.mapMap = mapMap;
        return this;
    }

    public List<Map<Integer, MessageParent>> getMapList() {
        return mapList;
    }

    public Message setMapList(List<Map<Integer, MessageParent>> mapList) {
        this.mapList = mapList;
        return this;
    }

    public MessageEnum getE1() {
        return e1;
    }

    public Message setE1(MessageEnum e1) {
        this.e1 = e1;
        return this;
    }

    public MessageEnum getE2() {
        return e2;
    }

    public Message setE2(MessageEnum e2) {
        this.e2 = e2;
        return this;
    }

    public Object getO1() {
        return o1;
    }

    public Message setO1(Object o1) {
        this.o1 = o1;
        return this;
    }

    public Object getO2() {
        return o2;
    }

    public Message setO2(Object o2) {
        this.o2 = o2;
        return this;
    }

    public Object getO3() {
        return o3;
    }

    public Message setO3(Object o3) {
        this.o3 = o3;
        return this;
    }

    public Object getO4() {
        return o4;
    }

    public Message setO4(Object o4) {
        this.o4 = o4;
        return this;
    }

    public Object getO5() {
        return o5;
    }

    public Message setO5(Object o5) {
        this.o5 = o5;
        return this;
    }

    public Object getO6() {
        return o6;
    }

    public Message setO6(Object o6) {
        this.o6 = o6;
        return this;
    }

    public Object getO7() {
        return o7;
    }

    public Message setO7(Object o7) {
        this.o7 = o7;
        return this;
    }

    public Object getO8() {
        return o8;
    }

    public Message setO8(Object o8) {
        this.o8 = o8;
        return this;
    }

    public Object getO9() {
        return o9;
    }

    public Message setO9(Object o9) {
        this.o9 = o9;
        return this;
    }

    public Object getO10() {
        return o10;
    }

    public Message setO10(Object o10) {
        this.o10 = o10;
        return this;
    }

    public Object getO11() {
        return o11;
    }

    public Message setO11(Object o11) {
        this.o11 = o11;
        return this;
    }

    public Object getO12() {
        return o12;
    }

    public Message setO12(Object o12) {
        this.o12 = o12;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        if (!super.equals(o)) return false;
        Message message = (Message) o;
        return Arrays.equals(ints, message.ints) && Arrays.equals(messageParents, message.messageParents) &&
                Objects.equals(messageParentList, message.messageParentList) &&
                Objects.equals(messageParentSet, message.messageParentSet) &&
                Objects.equals(beanMap, message.beanMap) && Arrays.deepEquals(intInts, message.intInts) &&
                Arrays.deepEquals(beanMessageParents, message.beanMessageParents) &&
                Objects.equals(listList, message.listList) && Objects.equals(setSet, message.setSet) &&
                Objects.equals(mapMap, message.mapMap) && Objects.equals(mapList, message.mapList) &&
                e1 == message.e1 && e2 == message.e2 && Objects.equals(o1, message.o1) &&
                Objects.equals(o2, message.o2) &&
                Arrays.equals((Object[])o3, (Object[])message.o3) &&
                Objects.equals(o4, message.o4) && Objects.equals(o5, message.o5) &&
                Objects.equals(o6, message.o6) &&
                Arrays.deepEquals((Object[]) o7, (Object[]) message.o7) &&
                Arrays.deepEquals((Object[]) o8, (Object[]) message.o8) &&
                Objects.equals(o9, message.o9) &&
                Objects.equals(o10, message.o10) && Objects.equals(o11, message.o11) &&
                Objects.equals(o12, message.o12);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), messageParentList, messageParentSet, beanMap, listList, setSet, mapMap, mapList,
                e1, e2, o1, o2, o4, o5, o6, o9, o10, o11, o12);
        result = 31 * result + Arrays.hashCode(ints);
        result = 31 * result + Arrays.hashCode(messageParents);
        result = 31 * result + Arrays.deepHashCode(intInts);
        result = 31 * result + Arrays.deepHashCode(beanMessageParents);
        return result;
    }

    @Override
    public String toString() {
        return "Message{" +
                "ints=" + Arrays.toString(ints) +
                ", messageParents=" + Arrays.toString(messageParents) +
                ", messageParentList=" + messageParentList +
                ", messageParentSet=" + messageParentSet +
                ", beanMap=" + beanMap +
                ", intInts=" + Arrays.deepToString(intInts) +
                ", beanMessageParents=" + Arrays.deepToString(beanMessageParents) +
                ", listList=" + listList +
                ", setSet=" + setSet +
                ", mapMap=" + mapMap +
                ", mapList=" + mapList +
                ", e1=" + e1 +
                ", e2=" + e2 +
                ", o1=" + o1 +
                ", o2=" + o2 +
                ", o3=" + Arrays.toString((Object[]) o3) +
                ", o4=" + o4 +
                ", o5=" + o5 +
                ", o6=" + o6 +
                ", o7=" +  Arrays.deepToString((Object[]) o7) +
                ", o8=" + Arrays.deepToString((Object[]) o8) +
                ", o9=" + o9 +
                ", o10=" + o10 +
                ", o11=" + o11 +
                ", o12=" + o12 +
                ", a=" + a +
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
                "} ";
    }
}
