package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
 final class ByteSchema extends PolymorphicSchema{
    /** 单例 */
     static final ByteSchema INSTANCE = new ByteSchema();

    private ByteSchema() {
    }

    @Override
    public Object read(Input input) {
        return (byte)input.readByte();
    }

    @Override
    public void write(Output output, Object o) {
        output.writeByte((byte) o);
    }
}
