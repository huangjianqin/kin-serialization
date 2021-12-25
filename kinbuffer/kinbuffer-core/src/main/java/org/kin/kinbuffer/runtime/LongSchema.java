package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
final class LongSchema extends PolymorphicSchema{
    /** 单例 */
    static final LongSchema INSTANCE = new LongSchema();

    private LongSchema() {
    }

    @Override
    public Object read(Input input) {
        return input.readInt64();
    }

    @Override
    public void write(Output output, Object o) {
        output.writeInt64((long) o);
    }
}
