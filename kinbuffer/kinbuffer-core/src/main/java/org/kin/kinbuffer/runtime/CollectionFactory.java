package org.kin.kinbuffer.runtime;

import org.kin.framework.utils.SPI;

import java.util.Collection;

/**
 * 集合工厂类
 * @author huangjianqin
 * @date 2021/12/24
 */
@SPI
@FunctionalInterface
public interface CollectionFactory<C extends Collection<?>> extends Factory{
    /**
     * 创建{@link Collection}实例
     */
    C newCollection();
}
