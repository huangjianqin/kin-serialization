package org.kin.serialization;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.instancio.Instancio;
import org.kin.framework.utils.ExtensionLoader;

import java.util.Arrays;

/**
 * @author huangjianqin
 * @date 2023/3/4
 */
public class SerializeManyTest {
    public static void main(String[] args) {
        Serialization serialization = ExtensionLoader.getExtension(Serialization.class, SerializationType.JSON.getCode());
//        Serialization serialization = ExtensionLoader.getExtension(Serialization.class, SerializationType.KIN_BUFFER.getCode());
//        Serialization serialization = ExtensionLoader.getExtension(Serialization.class, SerializationType.JSONB.getCode());
        M1 m1 = Instancio.create(M1.class);
        M2 m2 = Instancio.create(M2.class);
        M3 m3 = Instancio.create(M3.class);

        ByteBuf byteBuf = Unpooled.buffer();
        Object[] objects = {m1, m2, m3};
        serialization.serialize(byteBuf, objects);
        System.out.println(Arrays.deepToString(objects));
        System.out.println("-------------------");
        Object[] deObjects = serialization.deserialize(byteBuf, M1.class, M2.class, M3.class);
        System.out.println(Arrays.deepToString(deObjects));
    }
}
