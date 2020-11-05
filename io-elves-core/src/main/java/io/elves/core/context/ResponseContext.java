package io.elves.core.context;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

public class ResponseContext {
    private final byte[] bytes;
    private final HttpHeaders headers;
    private final HttpResponseStatus status;

    public ResponseContext(byte[] bytes, HttpHeaders headers) {
        this.bytes = bytes;
        this.headers = headers;
        this.status = HttpResponseStatus.OK;
    }

    public ResponseContext(byte[] bytes) {
        this.bytes = bytes;
        this.headers = null;
        this.status = HttpResponseStatus.OK;
    }

    public ResponseContext(byte[] bytes, HttpHeaders headers, HttpResponseStatus status) {
        this.bytes = bytes;
        this.headers = headers;
        this.status = status;
    }

    public ResponseContext(byte[] bytes, HttpResponseStatus status) {
        this.bytes = bytes;
        this.headers = null;
        this.status = status;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }
}
