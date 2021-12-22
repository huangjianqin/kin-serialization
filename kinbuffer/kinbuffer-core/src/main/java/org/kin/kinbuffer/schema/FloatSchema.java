package org.kin.kinbuffer.schema;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
public final class FloatSchema extends PolymorphicSchema{
    public static final FloatSchema INSTANCE = new FloatSchema();

    private FloatSchema() {
    }

    @Override
    public Object read(Input input) {
        return input.readFloat();
    }

    @Override
    public void write(Output output, Object o) {
        output.writeFloat((float) o);
    }
}
