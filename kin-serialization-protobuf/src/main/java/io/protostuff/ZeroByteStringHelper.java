package io.protostuff;

/**
 * {@link ByteString}相关api访问不到, 所以在io.protostuff package下创建该工具类, 充当桥接作用
 * Forked from <a href="https://github.com/fengjiachun/Jupiter">Jupiter</a>.
 * @author huangjianqin
 * @date 2021/11/27
 */
public final class ZeroByteStringHelper {
    public static byte[] getBytes(ByteString byteString) {
        return byteString.getBytes();
    }

    public static ByteString wrap(byte[] bytes) {
        return ByteString.wrap(bytes);
    }

    private ZeroByteStringHelper() {}
}
