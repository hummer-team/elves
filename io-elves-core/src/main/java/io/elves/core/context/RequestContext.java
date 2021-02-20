package io.elves.core.context;

import com.google.common.collect.ImmutableList;
import io.elves.core.coder.Coder;
import io.netty.handler.codec.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * this class is read only
 *
 * @author lee
 */
public final class RequestContext {
    private final byte[] body;
    private final HttpHeaders headers;
    private final String url;
    private final Map<String, Object> parameters;
    private final Coder decoder;

    public RequestContext(byte[] body
            , HttpHeaders headers
            , String url
            , Map<String, Object> parameters
            , Coder coder) {
        this.body = body;
        this.headers = headers;
        this.url = url;
        this.parameters = parameters;
        this.decoder = coder;
    }

    public Object getParam(String key) {
        return parameters == null ? null : parameters.get(key);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public String getUrl() {
        return url;
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

    public <T> T body(Class<T> clazz) {
        return decoder.decode(body, clazz, StandardCharsets.UTF_8);
    }

}
