package org.kin.kinbuffer.schema;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
public final class IntegerSchema implements PolymorphicSchema{
    public static final IntegerSchema INSTANCE = new IntegerSchema();

    private IntegerSchema() {
    }

    @Override
    public Object read(Input input) {
        return input.readInt();
    }

    @Override
    public void write(Output output, Object o) {
        output.writeInt((int) o);
    }
}
