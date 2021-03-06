package io.elves.http.server.handler;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import io.elves.core.coder.CoderContainer;
import io.elves.core.context.RequestContextInner;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.elves.core.ElvesConstants.TEXT_PLAIN_CODER;
import static io.elves.core.context.RequestContextInner.COMMAND_TARGET;

/**
 * @author lee
 */
public class RequestContextBuild {
    private RequestContextBuild() {

    }

    public static RequestContextInner parseRequest(FullHttpRequest request) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());

        return RequestContextInner
                .build()
                .uri(request.uri())
                .headers(request.headers())
                .requestContentType(getContentType(request.headers()))
                .decoder(CoderContainer.getCoder(getContentType(request.headers())))
                .responseContentType(getContentType(request.headers()))
                .param(parseQueryParam(queryStringDecoder))
                .param(parsePostParam(request))
                .body(parseBodyByte(request))
                .method(request.method().name())
                .metadata(COMMAND_TARGET, parseTarget(queryStringDecoder.rawPath(), request.method()))
                .builder();

    }

    private static String getContentType(HttpHeaders headers) {
        String contentType = headers.get("Content-Type");
        if (Strings.isNullOrEmpty(contentType)) {
            return TEXT_PLAIN_CODER;
        }
        return Iterables.get(Splitter.on(";").split(contentType), 0);
    }

    private static String parseTarget(String uri, HttpMethod method) {
        if (StringUtils.isEmpty(uri)) {
            return String.format("-%s", method);
        }
        return String.format("%s-%s", method, uri);
    }

    private static byte[] parseBodyByte(FullHttpRequest request) {
        // Parse body.
        if (request.content().readableBytes() <= 0) {
            return null;
        } else {
            byte[] body = new byte[request.content().readableBytes()];
            request.content().getBytes(0, body);
            return body;
        }
    }

    private static Map<String, String> parsePostParam(FullHttpRequest request) {
        // Deal with post method, parameter in post has more privilege compared to that in querystring
        Map<String, String> map = new ConcurrentHashMap<>(16);
        if (request.method().equals(HttpMethod.POST)) {
            // support multi-part and form-urlencoded
            HttpPostRequestDecoder postRequestDecoder = null;
            try {
                postRequestDecoder = new HttpPostRequestDecoder(request);
                for (InterfaceHttpData data : postRequestDecoder.getBodyHttpDatas()) {
                    data.retain(); // must retain each attr before destroy
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                        if (data instanceof HttpData) {
                            HttpData httpData = (HttpData) data;
                            try {
                                String name = httpData.getName();
                                String value = httpData.getString();
                                map.put(name, value);
                            } catch (IOException e) {
                                //
                            }
                        }
                    }
                }
            } finally {
                if (postRequestDecoder != null) {
                    postRequestDecoder.destroy();
                }
            }
        }
        return map;
    }

    private static Map<String, String> parseQueryParam(QueryStringDecoder queryStringDecoder) {
        Map<String, List<String>> paramMap = queryStringDecoder.parameters();
        Map<String, String> temp = new ConcurrentHashMap<>(16);
        // Parse request parameters.
        if (!paramMap.isEmpty()) {
            for (Map.Entry<String, List<String>> p : paramMap.entrySet()) {
                if (!p.getValue().isEmpty()) {
                    temp.put(p.getKey(), p.getValue().get(0));
                }
            }
        }
        return temp;
    }
}
