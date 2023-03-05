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
public class GenericImpl1 extends GenericParent{
    private static final long serialVersionUID = -8930711263194380853L;

    private Integer b;
    private List<Long> c;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenericImpl1)) return false;
        GenericImpl1 that = (GenericImpl1) o;
        return Objects.equals(b, that.b) && Objects.equals(c, that.c);
    }

    @Override
    public int hashCode() {
        return Objects.hash(b, c);
    }

    @Override
    public String toString() {
        return "GenericImpl1{" +
                "b=" + b +
                ", c=" + c +
                "} " + super.toString();
    }
}
