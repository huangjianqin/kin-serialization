package org.kin.kinbuffer.runtime;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import org.kin.kinbuffer.io.Input;
import org.kin.kinbuffer.io.Output;

/**
 * enum schema
 *
 * @author huangjianqin
 * @date 2021/12/20
 */
 class EnumSchema<E extends Enum<E>> extends PolymorphicSchema<E> {
    /** enum class */
    private final Class<E> enumClass;
    /** 是否将enum序列化成enum ordinal, 否则序列化成enum name */
    private final boolean toOrdinal;
    /** key -> enum name , value -> enum instance */
    private final BiMap<String, E> name2Inst;
    /** key -> enum ordinal , value -> enum instance */
    private final BiMap<Integer, E> id2Inst;

    public EnumSchema(Class<E> enumClass){
        this(enumClass, true);
    }

    /**
     * @param toOrdinal 是否将enum序列化成enum ordinal, 否则序列化成enum name
     */
    public EnumSchema(Class<E> enumClass, boolean toOrdinal) {
        this.enumClass = enumClass;
        this.toOrdinal = toOrdinal;

        ImmutableBiMap.Builder<String, E> name2InstBuilder = ImmutableBiMap.builder();
        ImmutableBiMap.Builder<Integer, E> id2InstBuilder = ImmutableBiMap.builder();
        for (E instance : enumClass.getEnumConstants()) {
            id2InstBuilder.put(instance.ordinal(), instance);
            name2InstBuilder.put(instance.name(), instance);
        }

        name2Inst = name2InstBuilder.build();
        id2Inst = id2InstBuilder.build();
    }

    @Override
    public E read(Input input) {
        if(toOrdinal){
            //enum id
            int enumId = input.readInt32();
            return id2Inst.get(enumId);
        }
        else {
            //enum name
            String name = input.readString();
            return Enum.valueOf(enumClass, name);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(Output output, E e) {
        if(toOrdinal){
            //enum id
            int enumId = id2Inst.inverse().get(e);
            output.writeInt32(enumId);
        }
        else {
            //enum name
            String enumName = e.name();
            output.writeString(enumName);
        }
    }
}
