package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
public final class StringSchema extends PolymorphicSchema{
    public static final StringSchema INSTANCE = new StringSchema();

    private StringSchema() {
    }

    @Override
    public Object read(Input input) {
        return input.readString();
    }

    @Override
    public void write(Output output, Object o) {
        output.writeString((String) o);
    }
}
