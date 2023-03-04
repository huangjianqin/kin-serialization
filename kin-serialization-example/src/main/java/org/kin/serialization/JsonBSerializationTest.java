package org.kin.serialization;

/**
 * @author huangjianqin
 * @date 2023/3/4
 */
public class JsonBSerializationTest {
    public static void main(String[] args) {
        SerializeTestBase
                .builder(SerializationType.JSONB)
                .run();
    }
}
