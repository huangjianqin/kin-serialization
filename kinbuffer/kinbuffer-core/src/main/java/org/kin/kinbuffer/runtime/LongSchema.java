package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
final class LongSchema extends PolymorphicSchema<Long>{
    /** 单例 */
    static final LongSchema INSTANCE = new LongSchema();

    private LongSchema() {
    }

    @Override
    public Long read(Input input) {
        return input.readVarInt64();
    }

    @Override
    public void write(Output output, Long l) {
        output.writeVarInt64(l);
    }
}
