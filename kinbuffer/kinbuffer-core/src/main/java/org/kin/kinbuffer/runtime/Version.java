package org.kin.kinbuffer.runtime;

import java.lang.annotation.*;

/**
 * 序列化class版本定义, 一般配合{@link Since}一起使用
 * @author huangjianqin
 * @date 2023/1/11
 * @see Since
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Version {
    /**
     * 版本号定义
     * @return  版本号
     */
    int value();
}
