package io.elves.core.context;

import com.google.common.collect.ImmutableList;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.Collections;
import java.util.List;

/**
 * this class is read only
 *
 * @author lee
 */
public final class RequestContext {
    private final byte[] body;
    private final HttpHeaders headers;

    public RequestContext(byte[] body, HttpHeaders headers) {
        this.body = body;
        this.headers = headers;
    }

    public final byte[] getBody() {
        return body;
    }

    public final HttpHeaders getHeaders() {
        return headers;
    }

    public final String getHeader(final String name) {
        return headers == null ? "" : headers.get(name);
    }

    public final List<String> getHeaderAll(final String name) {
        return headers == null ? Collections.emptyList() : ImmutableList.copyOf(headers.getAll(name));
    }
}
