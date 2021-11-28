package org.kin.serialization.hessian2;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.serialization.Serialization;
import org.kin.serialization.SerializationType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by 健勤 on 2017/2/9.
 */
@Extension(value = "hessian2", code = 3)
public class Hessian2Serialization implements Serialization {
    @Override
    public byte[] serialize(Object target) {
        // TODO: 2021/11/28  统一加上类型检查
        if (target == null) {
            throw new NullPointerException("Serialized object must be not null");
        }


        if (!(target instanceof Serializable)) {
            throw new IllegalStateException("Serialized class " + target.getClass().getSimpleName() + " must implement java.io.Serializable");
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Hessian2Output hessian2Output = new Hessian2Output(outputStream);
            hessian2Output.writeObject(target);

            hessian2Output.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> targetClass) {
        if (bytes == null || bytes.length <= 0) {
            throw new IllegalStateException("byte array must be not null or it's length must be greater than zero");
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            Hessian2Input hessian2Input = new Hessian2Input(inputStream);
            T ret = (T) hessian2Input.readObject(targetClass);
            hessian2Input.close();

            return ret;
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        //理论上不会到这里
        throw new IllegalStateException("encounter unknown error");
    }

    @Override
    public int type() {
        return SerializationType.HESSIAN2.getCode();
    }
}
