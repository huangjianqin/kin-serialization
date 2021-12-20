package org.kin.kinbuffer.schema;

import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2021/12/20
 */
public class EnumSchema<E extends Enum<E>> implements PolymorphicSchema{
    private final Class<E> enumClass;

    public EnumSchema(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public Object read(Input input) {
        //enum name
        String name = input.readString();
        return Enum.valueOf(enumClass, name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(Output output, Object o) {
        String enumName = ((Enum<E>) o).name();
        output.writeString(enumName);
    }
}
