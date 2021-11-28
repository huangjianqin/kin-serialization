package org.kin.serialization;

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

    public MessageParent[][] getBeanMessageParents() {
        return beanMessageParents;
    }

    public Message setBeanMessageParents(MessageParent[][] beanMessageParents) {
        this.beanMessageParents = beanMessageParents;
        return this;
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
        result = 31 * result + Arrays.deepHashCode(intInts);
        result = 31 * result + Arrays.deepHashCode(beanMessageParents);
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
