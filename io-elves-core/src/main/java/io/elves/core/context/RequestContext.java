package io.elves.core.context;

import io.elves.core.encoder.Decoder;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Command request representation of command center.
 *
 * @author lee
 */
public class RequestContext {

    public static final String COMMAND_TARGET = "command-target";
    private final Map<String, String> metadata = new HashMap<String, String>();
    private final Map<String, String> parameters = new HashMap<String, String>();
    private HttpHeaders headers;
    private String contentType;
    private byte[] body;
    private Decoder decoder;

    public static RequestContext build() {
        return new RequestContext();
    }

    public <T> T body(Class<?> clazz) {
        if (decoder == null) {
            throw new IllegalArgumentException("decoder not found");
        }
        return decoder.decode(getBodyByte(), clazz);
    }

    public Decoder getDecoder() {
        return decoder;
    }

    public RequestContext decoder(Decoder decoder) {
        this.decoder = decoder;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public RequestContext contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public byte[] getBodyByte() {
        return body;
    }

    public RequestContext body(byte[] body) {
        this.body = body;
        return this;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParam(String key) {
        return parameters.get(key);
    }

    public String getParam(String key, String defaultValue) {
        String value = parameters.get(key);
        return StringUtils.isBlank(value) ? defaultValue : value;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public String getCommandName() {
        return getMetadata().get(COMMAND_TARGET);
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public RequestContext headers(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    public RequestContext param(String key, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("Parameter key cannot be empty");
        }
        parameters.put(key, value);
        return this;
    }

    public RequestContext param(Map<String, String> paramMap) {
        if (paramMap == null) {
            return this;
        }
        parameters.putAll(paramMap);
        return this;
    }

    public RequestContext metadata(String key, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("Metadata key cannot be empty");
        }
        metadata.put(key, value);
        return this;
    }

    public RequestContext builder() {
        //todo check
        return this;
    }

    public void clean() {
        metadata.clear();
        parameters.clear();
        headers.clear();
        body = null;
    }
}
