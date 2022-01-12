package org.kin.serialization.avro;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.kin.framework.io.ByteBufferInputStream;
import org.kin.framework.io.ScalableByteBufferOutputStream;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.serialization.AbstractSerialization;
import org.kin.serialization.OutputStreams;
import org.kin.serialization.Serialization;
import org.kin.serialization.SerializationType;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * avro schema基于json, 性能稍微比pb好, 支持动态, 压缩二进制
 * @author huangjianqin
 * @date 2020/11/27
 */
@Extension(value = "avro", code = 7)
public final class AvroSerialization extends AbstractSerialization {
    /** encoder */
    private final EncoderFactory encoderFactory = EncoderFactory.get();
    /** decoder */
    private final DecoderFactory decoderFactory = DecoderFactory.get();

    @Override
    protected byte[] serialize0(Object target) {
        ByteArrayOutputStream baos = OutputStreams.getByteArrayOutputStream();
        serialize1(baos, target);
        return baos.toByteArray();
    }

    @Override
    protected <T> ByteBuffer serialize0(ByteBuffer byteBuffer, T target) {
        ScalableByteBufferOutputStream ebbos = new ScalableByteBufferOutputStream(byteBuffer);
        serialize1(ebbos, target);
        return ebbos.getSink();
    }

    @Override
    protected <T> void serialize0(ByteBuf byteBuf, T target) {
        serialize1(new ByteBufOutputStream(byteBuf), target);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> void serialize1(OutputStream os, T target){
        try{
            BinaryEncoder encoder = encoderFactory.binaryEncoder(os, null);

            ReflectDatumWriter dd = new ReflectDatumWriter<>(target.getClass());
            dd.write(target, encoder);

            encoder.flush();
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }finally {
            try {
                os.close();
            } catch (IOException e) {
                ExceptionUtils.throwExt(e);
            }
        }
    }

    @Override
    protected  <T> T deserialize0(byte[] bytes, Class<T> targetClass) {
        return deserialize1(new ByteArrayInputStream(bytes), targetClass);
    }

    @Override
    protected <T> T deserialize0(ByteBuffer byteBuffer, Class<T> targetClass) {
        return deserialize1(new ByteBufferInputStream(byteBuffer), targetClass);
    }

    @Override
    protected <T> T deserialize0(ByteBuf byteBuf, Class<T> targetClass) {
        return deserialize1(new ByteBufInputStream(byteBuf), targetClass);
    }

    protected <T> T deserialize1(InputStream is, Class<T> targetClass){
        //不支持不确定类型的反序列化
        BinaryDecoder decoder = decoderFactory.binaryDecoder(is, null);

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
