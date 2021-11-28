package org.kin.serialization;

import java.io.Serializable;
import java.util.Objects;

/**
 * 序列化测试的类
 *
 * @author huangjianqin
 * @date 2020/11/27
 */
public class Message implements Serializable {
    private static final long serialVersionUID = -3399588049581257541L;

    private int a;
    private String b;
    private Object copy;

    public Message() {
    }

    public Message(int a, String b, Object copy) {
        this.a = a;
        this.b = b;
        this.copy = copy;
    }

    public int getC() {
        return 1;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public Object getCopy() {
        return copy;
    }

    public void setCopy(Object copy) {
        this.copy = copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message message = (Message) o;
        return a == message.a && Objects.equals(b, message.b) && Objects.equals(copy, message.copy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, copy);
    }

    @Override
    public String toString() {
        return "Type{" +
                "a=" + a +
                ", b='" + b + '\'' +
                ", copy=" + copy +
                '}';
    }
}
