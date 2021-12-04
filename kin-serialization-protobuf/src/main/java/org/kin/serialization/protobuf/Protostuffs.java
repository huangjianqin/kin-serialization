package org.kin.serialization.protobuf;

import org.kin.framework.utils.SysUtils;

/**
 * @author huangjianqin
 * @date 2021/12/4
 */
public final class Protostuffs {
    /** 是否启动protostuff zigzag */
    public static final boolean ZIGZAG;

    static {
        ZIGZAG = SysUtils.getBoolSysProperty("kin.serialization.protostuff.zigzag", false);
        int a = 1;
    }

    private Protostuffs() {
    }
}
