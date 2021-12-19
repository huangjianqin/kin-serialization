package org.kin.kinbuffer.schema;

/**
 * @author huangjianqin
 * @date 2021/12/19
 */
@SuppressWarnings("rawtypes")
public abstract class NestSchema<T> implements Schema<T>{
    protected final Schema schema;

    protected NestSchema(Schema schema) {
        this.schema = schema;
    }
}
