package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
final class DoubleSchema extends PolymorphicSchema {
    /** 单例 */
    static final DoubleSchema INSTANCE = new DoubleSchema();

    private DoubleSchema() {
    }

    @Override
    public Object read(Input input) {
        return input.readDouble();
    }

    @Override
    public void write(Output output, Object o) {
        output.writeDouble((double) o);
    }
}
