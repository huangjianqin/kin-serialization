package org.kin.kinbuffer.runtime;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * @author huangjianqin
 * @date 2021/12/21
 */
 final class CharacterSchema extends PolymorphicSchema<Character>{
    /** 单例 */
     static final CharacterSchema INSTANCE = new CharacterSchema();

    private CharacterSchema() {
    }

    @Override
    public Character read(Input input) {
        return (char) input.readInt32();
    }

    @Override
    public void write(Output output, Character character) {
        output.writeInt32(character);
    }
}
