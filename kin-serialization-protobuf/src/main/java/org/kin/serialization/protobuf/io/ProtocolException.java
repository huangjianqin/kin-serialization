package org.kin.serialization.protobuf.io;

import io.protostuff.ProtobufException;

/**
 * Forked from <a href="https://github.com/fengjiachun/Jupiter">Jupiter</a>.
 * @author huangjianqin
 * @date 2021/11/27
 */
public final class ProtocolException extends ProtobufException {

    private static final long serialVersionUID = 8548555815656145587L;

    public ProtocolException(String description) {
        super(description);
    }

    public ProtocolException(String description, Throwable cause) {
        super(description, cause);
    }

    static ProtocolException misreportedSize() {
        return new ProtocolException(
                "CodedInput encountered an embedded string or bytes " +
                        "that misreported its size.");
    }

    static ProtocolException negativeSize() {
        return new ProtocolException(
                "CodedInput encountered an embedded string or message " +
                        "which claimed to have negative size.");
    }

    static ProtocolException malformedVarInt() {
        return new ProtocolException(
                "CodedInput encountered a malformed varint.");
    }

    static ProtocolException invalidTag() {
        return new ProtocolException(
                "Protocol message contained an invalid tag (zero).");
    }

    static ProtocolException invalidEndTag() {
        return new ProtocolException(
                "Protocol message end-group tag did not match expected tag.");
    }

    static ProtocolException invalidWireType() {
        return new ProtocolException(
                "Protocol message tag had invalid wire type.");
    }
}

