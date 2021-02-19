package io.elves.core.context;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import io.elves.core.coder.Coder;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.elves.core.ElvesConstants.FORM_DATA_CODER;
import static io.elves.core.ElvesConstants.TEXT_PLAIN_CODER;
import static io.elves.core.ElvesConstants.X_WWW_FORM_URLENCODED;

/**
 * Command request representation of command center.
 *
 * @author lee
 */
public class RequestContextInner {

    public static final String COMMAND_TARGET = "command-target";
    private final Map<String, String> metadata = new ConcurrentHashMap<String, String>();
    private final Map<String, Object> parameters = new ConcurrentHashMap<String, Object>();
    private HttpHeaders headers;
    private String requestContentType;
    private String responseContentType;
    private byte[] body;
    private String method;
    private Coder decoder;
    private String uri;

    public static RequestContextInner build() {
        return new RequestContextInner();
    }

    public String getUri() {
        return uri;
    }

    public RequestContextInner uri(String uri) {
        this.uri = uri;
        return this;
    }

    public <T> T body(Class<T> clazz) {
        return getDecoder().decode(getBodyByte(), clazz, StandardCharsets.UTF_8);
    }

    public <T> T bodyForParameter(Class<T> clazz) {
        return getDecoder().decode(JSON.toJSONBytes(getParameters()), clazz, StandardCharsets.UTF_8);
    }

    public Coder getDecoder() {
        if (decoder == null) {
            throw new IllegalArgumentException("request " + uri + " decoder not found");
        }
        return decoder;
    }

    public RequestContextInner decoder(Coder decoder) {
        this.decoder = decoder;
        return this;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public RequestContextInner responseContentType(String requestContentType) {
        if (FORM_DATA_CODER.equals(requestContentType)) {
            this.responseContentType = TEXT_PLAIN_CODER;
            return this;
        }

        if (X_WWW_FORM_URLENCODED.equals(requestContentType)) {
            this.responseContentType = TEXT_PLAIN_CODER;
            return this;
        }
        this.responseContentType = requestContentType;
        return this;
    }

    public String getRequestContentType(boolean ifNullUseDefault) {
        return Strings.isNullOrEmpty(requestContentType) && ifNullUseDefault ?
                TEXT_PLAIN_CODER : requestContentType;
    }

    public RequestContextInner requestContentType(String contentType) {
        this.requestContentType = contentType;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public RequestContextInner method(String method) {
        this.method = method;
        return this;
    }

    public byte[] getBodyByte() {
        return body;
    }

    public RequestContextInner body(byte[] body) {
        this.body = body;
        return this;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Object getParam(String key) {
        return parameters.get(key);
    }

    public Object getParam(String key, Object defaultValue) {
        Object value = parameters.get(key);
        return value==null ? defaultValue : value;
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

    public RequestContextInner headers(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    public RequestContextInner param(String key, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("Parameter key cannot be empty");
        }
        parameters.put(key, value);
        return this;
    }

    public RequestContextInner param(Map<String, String> paramMap) {
        if (paramMap == null) {
            return this;
        }
        parameters.putAll(paramMap);
        return this;
    }

    public RequestContextInner metadata(String key, String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("Metadata key cannot be empty");
        }
        metadata.put(key, value);
        return this;
    }

    public RequestContextInner builder() {
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
