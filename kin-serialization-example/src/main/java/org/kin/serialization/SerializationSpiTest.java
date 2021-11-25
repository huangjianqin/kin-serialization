package org.kin.serialization;

import org.kin.framework.utils.ExtensionLoader;

/**
 * @author huangjianqin
 * @date 2020/9/27
 */
public class SerializationSpiTest {
    public static void main(String[] args) {
        ExtensionLoader loader = ExtensionLoader.load();
        System.out.println(loader.getExtension(Serialization.class, "my"));
    }
}
