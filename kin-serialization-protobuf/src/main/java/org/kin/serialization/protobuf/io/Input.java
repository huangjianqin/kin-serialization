package org.kin.serialization.protobuf.io;

import io.netty.buffer.ByteBuf;

/**
 * @author huangjianqin
 * @date 2023/3/3
 */
public interface Input extends io.protostuff.Input {
    /**
     * 修正{@link ByteBuf#writerIndex()}
     * 因为底层使用{@link ByteBuf#nioBuffer()}获取{@link java.nio.ByteBuffer}实例,
     * 但修改{@link java.nio.ByteBuffer}实例, 对{@link io.netty.buffer.ByteBuf}不可见,
     * 故完成output后需要修正{@link ByteBuf#writerIndex()}
     */
    void fixByteBufReadIndex();
}
