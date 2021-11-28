package org.kin.serialization;

/**
 * @author huangjianqin
 * @date 2021/11/28
 */
public class JavaSerializationTest {
    public static void main(String[] args) {
        SerializeTestBase
                .builder(SerializationType.JAVA)
                .run();
    }
}
