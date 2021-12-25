package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
final class ShortSchema extends PolymorphicSchema{
    /** 单例 */
    static final ShortSchema INSTANCE = new ShortSchema();

    private ShortSchema() {
    }

    @Override
    public Object read(Input input) {
        return (short) input.readInt32();
    }

    @Override
    public void write(Output output, Object o) {
        output.writeInt32((short) o);
    }
}
