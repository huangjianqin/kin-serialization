package org.kin.kinbuffer.schema;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
public final class ShortSchema implements PolymorphicSchema{
    public static final ShortSchema INSTANCE = new ShortSchema();

    private ShortSchema() {
    }

    @Override
    public Object read(Input input) {
        return (short) input.readInt();
    }

    @Override
    public void write(Output output, Object o) {
        output.writeInt((short) o);
    }
}
