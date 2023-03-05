package org.kin.kinbuffer;

import org.kin.kinbuffer.runtime.UseGeneric;

import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2023/3/5
 */
public class GenericMessage {
    @UseGeneric
    private GenericParent gp;
    @UseGeneric
    private GenericParent gp1;

    public GenericParent getGp() {
        return gp;
    }

    public void setGp(GenericParent gp) {
        this.gp = gp;
    }

    public GenericParent getGp1() {
        return gp1;
    }

    public void setGp1(GenericParent gp1) {
        this.gp1 = gp1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenericMessage)) return false;
        GenericMessage message = (GenericMessage) o;
        return Objects.equals(gp, message.gp) && Objects.equals(gp1, message.gp1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gp, gp1);
    }

    @Override
    public String toString() {
        return "GenericMessage{" +
                "gp=" + gp +
                ", gp1=" + gp1 +
                '}';
    }
}
