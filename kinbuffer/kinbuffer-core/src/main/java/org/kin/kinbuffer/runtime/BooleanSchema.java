package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
final class BooleanSchema extends PolymorphicSchema<Boolean> {
    /** 单例 */
    static final BooleanSchema INSTANCE = new BooleanSchema();

    private BooleanSchema() {
    }

    @Override
    public Boolean read(Input input) {
        return input.readBoolean();
    }

    @Override
    public void write(Output output, Boolean bool) {
        output.writeBoolean( bool);
    }
}
