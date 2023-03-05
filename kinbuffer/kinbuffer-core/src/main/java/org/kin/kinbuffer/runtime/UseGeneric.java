package org.kin.kinbuffer.runtime;

import java.lang.annotation.*;

/**
 * 标识序列化时使用泛化
 * @author huangjianqin
 * @date 2023/3/5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface UseGeneric {
}
