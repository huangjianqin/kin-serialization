package org.kin.serialization;

import java.io.IOException;

/**
 * @author huangjianqin
 * @date 2021/12/25
 */
public class KinbufferSerializationTest {
    public static void main(String[] args) throws IOException {
        SerializeTestBase
                .builder(SerializationType.KIN_BUFFER)
                .run(100000);
    }
}
