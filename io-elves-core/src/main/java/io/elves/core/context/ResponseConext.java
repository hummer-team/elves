package io.elves.core.context;

import io.netty.handler.codec.http.HttpHeaders;

public class ResponseConext {
    private final byte[] bytes;
    private final HttpHeaders headers;

    public ResponseConext(byte[] bytes, HttpHeaders headers) {
        this.bytes = bytes;
        this.headers = headers;
    }

    public ResponseConext(byte[] bytes) {
        this.bytes = bytes;
        this.headers = null;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }
}
