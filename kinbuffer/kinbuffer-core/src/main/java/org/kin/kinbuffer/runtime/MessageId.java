package org.kin.kinbuffer.runtime;

import java.lang.annotation.*;

/**
 * @author huangjianqin
 * @date 2022/4/22
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface MessageId {
    /** 定义message id */
    int value();
}
