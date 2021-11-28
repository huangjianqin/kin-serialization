package org.kin.serialization.gson;

import com.google.gson.Gson;
import org.kin.framework.utils.Extension;
import org.kin.serialization.AbstractSerialization;
import org.kin.serialization.Serialization;
import org.kin.serialization.SerializationType;

/**
 * @author huangjianqin
 * @date 2020/11/27
 */
@Extension(value = "gson", code = 6)
public final class GsonSerialization extends AbstractSerialization {
    private final Gson gson = new Gson();

    @Override
    protected byte[] serialize0(Object target) {
        return gson.toJson(target).getBytes();
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> targetClass) {
        //不支持不确定类型的反序列化
        return gson.fromJson(new String(bytes), targetClass);
    }

    @Override
    public int type() {
        return SerializationType.GSON.getCode();
    }
}
