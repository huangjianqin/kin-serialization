package org.kin.kinbuffer.runtime;

import java.lang.annotation.*;

/**
 * 泛化标识, 定义绑定实现类与其code
 * @author huangjianqin
 * @date 2023/3/4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Generic {
    /**
     * 定义泛化实现类和其序列化的code
     */
    GenericType[] types();
}
