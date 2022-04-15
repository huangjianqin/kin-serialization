package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
final class IntegerSchema extends PolymorphicSchema<Integer>{
    /** 单例 */
    static final IntegerSchema INSTANCE = new IntegerSchema();

    private IntegerSchema() {
    }

    @Override
    public Integer read(Input input) {
        return input.readInt32();
    }

    @Override
    public void write(Output output, Integer i) {
        output.writeInt32(i);
    }
}
