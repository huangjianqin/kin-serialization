package org.kin.serialization.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.kin.framework.io.ByteBufferInputStream;
import org.kin.framework.io.ScalableByteBufferOutputStream;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.serialization.AbstractSerialization;
import org.kin.serialization.SerializationType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2019/7/29
 */
@Extension(value = "json", code = 4)
public final class JsonSerialization extends AbstractSerialization {
    private final ObjectMapper mapper = new ObjectMapper();
    private static final TypeReference<List<JsonNode>> TYPE_JSON_NODE_LIST = new TypeReference<List<JsonNode>>() {
    };

    /**
     * 按数组形式读取json, 然后再根据指定class反序列每个item
     */
    private static Object[] readJsonArray(ObjectMapper mapper, InputStream is, Class<?>[] targetClasses) {
        try {
            Object[] targets = new Object[targetClasses.length];
            List<JsonNode> jsonNodes = mapper.readValue(is, TYPE_JSON_NODE_LIST);
            for (int i = 0; i < targetClasses.length; i++) {
                targets[i] = mapper.treeToValue(jsonNodes.get(i), targetClasses[i]);
            }
            return targets;
        } catch (Exception e) {
            ExceptionUtils.throwExt(e);
        }

        return EMPTY_OBJECT_ARRAY;
    }

    public JsonSerialization() {
        //default true, 主要解决pojo中存在可能定义不确定类型的字段
        this(true);
    }

    /**
     * @param includeClassInfo json串包含类信息
     */
    public JsonSerialization(boolean includeClassInfo) {
        if (includeClassInfo) {
            //带上类型信息
            //resolved 解决接口参数(返回值)中包含Object类型时, json序列化与反序列化不一致问题, 这样子会增加数据传输的压力, 可通过数据压缩缓解
            mapper.activateDefaultTypingAsProperty(
                    LaissezFaireSubTypeValidator.instance,
                    ObjectMapper.DefaultTyping.NON_FINAL,
                    JsonTypeInfo.Id.MINIMAL_CLASS.getDefaultPropertyName());
        }

        mapper.findAndRegisterModules();

        //允许json中含有指定对象未包含的字段
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //允许序列化空对象
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        //不序列化默认值, 0,false,[],{}等等, 减少json长度
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_DEFAULT);
        //只认field, 那些get set is开头的方法不生成字段
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
    }

    @Override
    protected byte[] serialize0(Object target) {
        try {
            return mapper.writeValueAsBytes(target);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    @Override
    protected byte[] serializeMany(Object[] objects) {
        return serialize0(objects);
    }

    @Override
    protected <T> ByteBuffer serialize0(ByteBuffer byteBuffer, T target) {
        ScalableByteBufferOutputStream ebbos = new ScalableByteBufferOutputStream(byteBuffer);
        serialize1(ebbos, target);
        return ebbos.getSink();
    }

    @Override
    protected ByteBuffer serializeMany(ByteBuffer byteBuffer, Object[] objects) {
        return serialize0(byteBuffer, objects);
    }

    @Override
    protected <T> void serialize0(ByteBuf byteBuf, T target) {
        serialize1(new ByteBufOutputStream(byteBuf), target);
    }

    @Override
    protected void serializeMany(ByteBuf byteBuf, Object[] objects) {
        serialize0(byteBuf, objects);
    }

    private <T> void serialize1(OutputStream os, T target) {
        try {
            mapper.writeValue(os, target);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
    }

    @Override
    protected <T> T deserialize0(byte[] bytes, Class<T> targetClass) {
        try {
            return mapper.readValue(bytes, targetClass);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    @Override
    protected Object[] deserializeMany(byte[] bytes, Class<?>... targetClasses) {
        return readJsonArray(mapper, new ByteArrayInputStream(bytes), targetClasses);
    }

    @Override
    protected <T> T deserialize0(ByteBuffer byteBuffer, Class<T> targetClass) {
        return deserialize1(new ByteBufferInputStream(byteBuffer), targetClass);
    }

    @Override
    protected Object[] deserializeMany(ByteBuffer byteBuffer, Class<?>... targetClasses) {
        return readJsonArray(mapper, new ByteBufferInputStream(byteBuffer), targetClasses);
    }

    @Override
    protected <T> T deserialize0(ByteBuf byteBuf, Class<T> targetClass) {
        return deserialize1(new ByteBufInputStream(byteBuf), targetClass);
    }

    @Override
    protected Object[] deserializeMany(ByteBuf byteBuf, Class<?>... targetClasses) {
        return readJsonArray(mapper, new ByteBufInputStream(byteBuf), targetClasses);
    }

    private <T> T deserialize1(InputStream is, Class<T> targetClass) {
        try {
            return mapper.readValue(is, targetClass);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    @Override
    public int type() {
        return SerializationType.JSON.getCode();
    }
}
