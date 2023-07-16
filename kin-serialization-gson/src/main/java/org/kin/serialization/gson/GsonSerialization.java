package org.kin.serialization.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.kin.framework.utils.Extension;
import org.kin.serialization.AbstractSerialization;
import org.kin.serialization.Serialization;
import org.kin.serialization.SerializationType;

/**
 * 暂时还不支持字段类型为{@link Object}, 仅支持字段类型为接口或抽象类
 * @author huangjianqin
 * @date 2020/11/27
 */
@Extension(value = "gson", code = 6)
public final class GsonSerialization extends AbstractSerialization {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(RuntimeTypeAdapterFactory.INSTANCE)
            .create();

    @Override
    protected byte[] serialize0(Object target) {
        return gson.toJson(target).getBytes();
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> targetClass) {
        return gson.fromJson(new String(bytes), targetClass);
    }

    @Override
    public int type() {
        return SerializationType.GSON.getCode();
    }
}
