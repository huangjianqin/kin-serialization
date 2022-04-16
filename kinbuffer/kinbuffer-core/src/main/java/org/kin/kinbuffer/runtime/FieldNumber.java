package org.kin.kinbuffer.runtime;

import java.lang.annotation.*;

/**
 * @author huangjianqin
 * @date 2022/4/16
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface FieldNumber {
    /** 定义field number */
    int value();
}
