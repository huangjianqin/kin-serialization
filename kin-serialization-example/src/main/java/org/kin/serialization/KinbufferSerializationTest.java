package org.kin.serialization;

import org.kin.kinbuffer.runtime.Runtime;

import java.io.IOException;

/**
 * @author huangjianqin
 * @date 2021/12/25
 */
public class KinbufferSerializationTest {
    public static void main(String[] args) throws IOException {
        Runtime.registerMessageIdClass(201, Message.class);
        Runtime.registerMessageIdClass(202, MessageParent.class);

        SerializeTestBase
                .builder(SerializationType.KIN_BUFFER)
                .run();
    }
}
