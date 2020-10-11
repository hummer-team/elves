package io.elves.http.server.handler.http1;

import io.elves.core.context.RequestContext;
import io.elves.core.coder.CodecContainer;
import io.netty.handler.codec.http.FullHttpRequest;
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

import static io.elves.core.context.RequestContext.COMMAND_TARGET;

/**
 * @author lee
 */
public class BuildRequestContext {
    private BuildRequestContext() {

    }

    public static RequestContext parseRequest(FullHttpRequest request) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());

        return RequestContext
                .build()
                .headers(request.headers())
                .contentType(request.headers().get("Content-Type"))
                .decoder(CodecContainer.getCoder(request.headers().get("Content-Type")))
                .param(parseQueryParam(queryStringDecoder))
                .param(parsePostParam(request))
                .body(parseBodyByte(request))
                .metadata(COMMAND_TARGET, parseTarget(queryStringDecoder.rawPath(), request.method()))
                .builder();

    }

    private static String parseTarget(String uri, HttpMethod method) {
        if (StringUtils.isEmpty(uri)) {
            return String.format("-%s", method);
        }
        return String.format("%s-%s", uri, method);
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
        Map<String, String> temp = new ConcurrentHashMap<>();
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
