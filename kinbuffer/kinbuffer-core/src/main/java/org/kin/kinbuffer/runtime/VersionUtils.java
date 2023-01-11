package org.kin.kinbuffer.runtime;

/**
 * @author huangjianqin
 * @date 2023/1/11
 */
public final class VersionUtils {
    private VersionUtils() {
    }

    /**
     * 最小版本号
     */
    public static final int MIN_VERSION = 0;

    /**
     * 最大版本号
     */
    public static final int MAX_VERSION = Short.MAX_VALUE;

    /**
     * 检查版本号定义是否满足指定范围内
     * @param version   版本号
     */
    public static void checkVersion(int version){
        if(version <MIN_VERSION || version > MAX_VERSION){
            throw new IllegalStateException(String.format("version must be between %d and %d", 0, MAX_VERSION));
        }
    }
}
