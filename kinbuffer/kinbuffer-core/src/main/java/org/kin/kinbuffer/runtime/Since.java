package org.kin.kinbuffer.runtime;

import java.lang.annotation.*;

/**
 * 用于定义字段从哪个版本开始定义
 * @author huangjianqin
 * @date 2023/1/11
 * @see Version
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Since {
    /**
     * 版本号定义
     * @return  版本号
     */
    int value();
}
