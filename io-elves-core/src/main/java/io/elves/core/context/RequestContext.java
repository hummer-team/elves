package io.elves.core.context;

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
    private byte[] body;

    public byte[] getBody() {
        return body;
    }

    public RequestContext setBody(byte[] body) {
        this.body = body;
        return this;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public RequestContext addHeaders(HttpHeaders headers) {
        this.headers = headers;
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

    public RequestContext addParam(String key, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("Parameter key cannot be empty");
        }
        parameters.put(key, value);
        return this;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public RequestContext addMetadata(String key, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("Metadata key cannot be empty");
        }
        metadata.put(key, value);
        return this;
    }

    public String getCommandName() {
        return getMetadata().get(COMMAND_TARGET);
    }

    public void clean() {
        metadata.clear();
        parameters.clear();
        headers.clear();
        body = null;
    }
}
