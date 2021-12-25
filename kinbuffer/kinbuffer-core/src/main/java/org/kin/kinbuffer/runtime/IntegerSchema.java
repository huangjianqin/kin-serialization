package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
final class IntegerSchema extends PolymorphicSchema{
    /** 单例 */
    static final IntegerSchema INSTANCE = new IntegerSchema();

    private IntegerSchema() {
    }

    @Override
    public Object read(Input input) {
        return input.readInt32();
    }

    @Override
    public void write(Output output, Object o) {
        output.writeInt32((int) o);
    }
}
