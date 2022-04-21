package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
 final class ByteSchema extends PolymorphicSchema<Byte>{
    /** 单例 */
     static final ByteSchema INSTANCE = new ByteSchema();

    private ByteSchema() {
    }

    @Override
    public Byte read(Input input) {
        return input.readByte();
    }

    @Override
    public void write(Output output, Byte b) {
        output.writeByte( b);
    }
}
