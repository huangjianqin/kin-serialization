package org.custom.serialization;

import org.kin.framework.utils.Extension;
import org.kin.serialization.AbstractSerialization;
import org.kin.serialization.Serialization;

import java.io.IOException;

/**
 * 模拟外部使用自定义Serialization, 因此包名也做模拟
 *
 * @author huangjianqin
 * @date 2020/9/27
 */
@Extension(value = "my", code = 100)
public final class MySerialization extends AbstractSerialization {
    @Override
    protected <T> byte[] serialize0(T target) {
        return new byte[0];
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> targetClass) {
        return null;
    }

    @Override
    public int type() {
        return 10;
    }
}
