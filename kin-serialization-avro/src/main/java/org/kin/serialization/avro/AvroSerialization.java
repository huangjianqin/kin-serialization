package org.kin.serialization.avro;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.serialization.Serialization;
import org.kin.serialization.SerializationType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author huangjianqin
 * @date 2020/11/27
 */
@Extension(value = "avro", code = 7)
public class AvroSerialization implements Serialization {
    /** encoder */
    private final EncoderFactory encoderFactory = EncoderFactory.get();
    /** decoder */
    private final DecoderFactory decoderFactory = DecoderFactory.get();

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public byte[] serialize(Object target) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = encoderFactory.binaryEncoder(baos, null);

            ReflectDatumWriter dd = new ReflectDatumWriter<>(target.getClass());
            dd.write(target, encoder);

            encoder.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> targetClass) {
        //不支持不确定类型的反序列化
        BinaryDecoder decoder = decoderFactory.binaryDecoder(bytes, null);

        ReflectDatumReader<T> reader = new ReflectDatumReader<>(targetClass);
        try {
            return reader.read(null, decoder);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    @Override
    public int type() {
        return SerializationType.AVRO.getCode();
    }
}
