package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
final class StringSchema extends PolymorphicSchema<String>{
    /** 单例 */
    static final StringSchema INSTANCE = new StringSchema();

    private StringSchema() {
    }

    @Override
    public String read(Input input) {
        return input.readString();
    }

    @Override
    public void write(Output output, String s) {
        output.writeString(s);
    }
}
