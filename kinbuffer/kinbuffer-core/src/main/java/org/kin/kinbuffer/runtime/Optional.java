package org.kin.kinbuffer.runtime;

import java.lang.annotation.*;

/**
 * 标识字段是否可能为null, 如果是, 则需要写flag来标识是否为null
 * 对primitive类型无效
 * @author huangjianqin
 * @date 2023/3/4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Optional {
}
