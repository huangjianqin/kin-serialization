package org.kin.kinbuffer.schema;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
public final class CharacterSchema implements PolymorphicSchema{
    public static final CharacterSchema INSTANCE = new CharacterSchema();

    private CharacterSchema() {
    }

    @Override
    public Object read(Input input) {
        return (char) input.readInt();
    }

    @Override
    public void write(Output output, Object o) {
        output.writeInt((char) o);
    }
}
