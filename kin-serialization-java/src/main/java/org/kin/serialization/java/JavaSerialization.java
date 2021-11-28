package org.kin.serialization.java;

import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.serialization.Serialization;
import org.kin.serialization.SerializationType;

import java.io.*;

/**
 * @author 健勤
 * @date 2017/2/9
 */
@Extension(value = "java", code = 1)
public class JavaSerialization implements Serialization {
    @Override
    public byte[] serialize(Object target){
        if (target == null) {
            throw new NullPointerException("Serialized object must be not null");
        }

        if (!(target instanceof Serializable)) {
            throw new IllegalStateException("Serialized class " + target.getClass().getSimpleName() + " must implement java.io.Serializable");
        }

        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)){
            objectOutputStream.writeObject(target);
            return outputStream.toByteArray();
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> targetClass){
        if (bytes == null || bytes.length <= 0) {
            throw new IllegalStateException("byte array must be not null or it's length must be greater than zero");
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)){
            return (T)objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    @Override
    public int type() {
        return SerializationType.JAVA.getCode();
    }
}
