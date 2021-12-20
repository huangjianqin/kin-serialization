package org.kin.kinbuffer.schema;

import java.lang.annotation.*;

/**
 * @author huangjianqin
 * @date 2021/12/20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface MessageId {
    /**
     * 唯一标识message class的id, 用来替换class name, 以达到节省字节数的目的
     */
    int id();
}


