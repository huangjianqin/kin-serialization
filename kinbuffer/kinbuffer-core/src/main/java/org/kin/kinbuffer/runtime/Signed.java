package org.kin.kinbuffer.runtime;

import java.lang.annotation.*;

/**
 * 标识整形字段是有符号整形, 如果是有符号整形则使用zigzag+变长整形, 否则仅仅使用变长整形
 * zigzag对负数和小整形有明显优化
 * @author huangjianqin
 * @date 2021/12/22
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
@Documented
public @interface Signed {
}
