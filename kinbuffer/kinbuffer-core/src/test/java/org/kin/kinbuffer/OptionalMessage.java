package org.kin.kinbuffer;

import org.kin.kinbuffer.runtime.Optional;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author huangjianqin
 * @date 2023/3/4
 */
public class OptionalMessage implements Serializable {
    private static final long serialVersionUID = 1512610886580153867L;
    private Integer b;
    private List<Long> c;
    @Optional
    private Set<Integer> d;
    private Map<String, Integer> e;

    //setter && getter
    public Integer getB() {
        return b;
    }

    public void setB(Integer b) {
        this.b = b;
    }

    public List<Long> getC() {
        return c;
    }

    public void setC(List<Long> c) {
        this.c = c;
    }

    public Set<Integer> getD() {
        return d;
    }

    public void setD(Set<Integer> d) {
        this.d = d;
    }

    public Map<String, Integer> getE() {
        return e;
    }

    public void setE(Map<String, Integer> e) {
        this.e = e;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OptionalMessage)) return false;
        OptionalMessage that = (OptionalMessage) o;
        return Objects.equals(b, that.b) && Objects.equals(c, that.c) && Objects.equals(d, that.d) && Objects.equals(e, that.e);
    }

    @Override
    public int hashCode() {
        return Objects.hash(b, c, d, e);
    }

    @Override
    public String toString() {
        return "OptionMessage{" +
                ", b=" + b +
                ", c=" + c +
                ", d=" + d +
                ", e=" + e +
                '}';
    }
}
