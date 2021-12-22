package org.kin.kinbuffer.schema;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;
import org.kin.kinbuffer.schema.PolymorphicSchema;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
public final class BooleanSchema extends PolymorphicSchema {
    public static final BooleanSchema INSTANCE = new BooleanSchema();

    private BooleanSchema() {
    }

    @Override
    public Object read(Input input) {
        return input.readBoolean();
    }

    @Override
    public void write(Output output, Object o) {
        output.writeBoolean((Boolean) o);
    }
}
