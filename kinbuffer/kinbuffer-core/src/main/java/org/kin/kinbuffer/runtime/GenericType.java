package org.kin.kinbuffer.runtime;

import java.lang.annotation.*;

/**
 * @author huangjianqin
 * @date 2023/3/4
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GenericType {
    /**
     * type code
     * 会序列化, 用于识别实现类
     */
    int code();

    /**
     * type code对应的实现类
     */
    Class<?> type();
}
