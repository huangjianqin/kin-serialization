package org.kin.kinbuffer;

import org.kin.kinbuffer.runtime.Optional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author huangjianqin
 * @date 2023/3/5
 */
public class GenericImpl2 extends GenericParent{
    private static final long serialVersionUID = 8211740716190121239L;

    private Set<Integer> d;
    private Map<String, Integer> e;

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
        if (!(o instanceof GenericImpl2)) return false;
        GenericImpl2 that = (GenericImpl2) o;
        return Objects.equals(d, that.d) && Objects.equals(e, that.e);
    }

    @Override
    public int hashCode() {
        return Objects.hash(d, e);
    }

    @Override
    public String toString() {
        return "GenericImpl2{" +
                "d=" + d +
                ", e=" + e +
                "} " + super.toString();
    }
}
