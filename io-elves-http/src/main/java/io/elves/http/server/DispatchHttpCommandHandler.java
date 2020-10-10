package io.elves.http.server;


import io.elves.core.command.CommandHandlerContainer;
import io.elves.core.context.RequestContext;
import io.elves.core.encoder.CodecContainer;
import io.elves.core.encoder.Encoder;
import io.elves.core.handle.CommandHandle;
import io.elves.core.response.CommandResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static io.elves.core.context.RequestContext.COMMAND_TARGET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * Netty-based HTTP server handler for command center.
 * <p>
 * Note: HTTP chunked is not tested!
 *
 * @author leee
 */
@Slf4j
public class DispatchHttpCommandHandler extends SimpleChannelInboundHandler<Object> {
    /**
     * Calls {@link ChannelHandlerContext#fireExceptionCaught(Throwable)} to forward
     * to the next {@link io.netty.channel.ChannelHandler} in the {@link io.netty.channel.ChannelPipeline}.
     * <p>
     * Sub-classes may override this method to change behavior.
     *
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        log.error("dispatch http command exception ", cause);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        FullHttpRequest httpRequest = (FullHttpRequest) msg;
        RequestContext request = parseRequest(httpRequest);
        try {
            if (StringUtils.isBlank(request.getCommandName())) {
                writeErrorResponse(BAD_REQUEST.code(), "Invalid command", ctx);
                return;
            }
            handle(request, ctx, HttpUtil.isKeepAlive(httpRequest));
        } catch (Throwable ex) {
            writeErrorResponse(INTERNAL_SERVER_ERROR.code(), ex.getMessage(), ctx);
            log.warn("Internal error", ex);
        } finally {
            request.clean();
        }
    }

    private void handle(RequestContext request, ChannelHandlerContext ctx, boolean keepAlive)
            throws Exception {
        CommandHandle<?> commandHandler = CommandHandlerContainer.getInstance().getHandle(request.getCommandName());
        if (commandHandler == null) {
            writeErrorResponse(NOT_FOUND.code(), String.format("not found -> \"%s\"", request.getCommandName()), ctx);
            return;
        }
        writeResponse(commandHandler.handle(request), request, ctx, keepAlive);
    }

    private Encoder<?> lookupEncoder(Class<?> clazz, String requestContextType) {
        if (clazz == null) {
            throw new IllegalArgumentException("Bad class metadata");
        }

        Encoder<?> encoder = CodecContainer.getEncoder(requestContextType);
        if (encoder == null) {
            log.error("command handle encoder {} not support", requestContextType);
            throw new IllegalArgumentException("Bad server encoder");
        }

        if (encoder.canEncode(clazz)) {
            return encoder;
        }

        return null;
    }

    private void writeErrorResponse(int statusCode, String message, ChannelHandlerContext ctx) {
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(statusCode),
                Unpooled.copiedBuffer(message, StandardCharsets.UTF_8));

        httpResponse.headers().set("Content-Type", "text/plain; charset=UTF-8");
        ctx.write(httpResponse);

        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    private byte[] getResponseByte(CommandResponse response, String requestContextType, ChannelHandlerContext ctx)
            throws Exception {
        byte[] body = new byte[]{};
        if (response.getCode() == 0) {
            if (response.getData() == null) {
                body = new byte[]{};
            } else {
                Encoder encoder = lookupEncoder(response.getData().getClass(), requestContextType);
                body = encoder.encode(response.getData());
            }
        } else {
            body = response.getExceptionMessage() != null
                    ? response.getExceptionMessage().getBytes("UTF-8")
                    : body;
        }
        return body;
    }

    private void writeResponse(CommandResponse response
            , RequestContext request
            , ChannelHandlerContext ctx, boolean keepAlive)
            throws Exception {

        byte[] body = getResponseByte(response, request.getHeaders().get("Content-Type"), ctx);

        HttpResponseStatus status = response.getCode() == 0 ? OK : BAD_REQUEST;

        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(body));

        httpResponse.headers().set("Content-Type", String.format("%s; charset=UTF-8"
                ,request.getHeaders().get("Content-Type")));
        httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
        if (keepAlive) {
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        } else {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
        ctx.write(httpResponse);
    }

    private RequestContext parseRequest(FullHttpRequest request) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        RequestContext serverRequest = new RequestContext();
        serverRequest.addHeaders(request.headers());

        Map<String, List<String>> paramMap = queryStringDecoder.parameters();
        // Parse request parameters.
        if (!paramMap.isEmpty()) {
            for (Entry<String, List<String>> p : paramMap.entrySet()) {
                if (!p.getValue().isEmpty()) {
                    serverRequest.addParam(p.getKey(), p.getValue().get(0));
                }
            }
        }
        // Deal with post method, parameter in post has more privilege compared to that in querystring
        if (request.method().equals(HttpMethod.POST)) {
            // support multi-part and form-urlencoded
            HttpPostRequestDecoder postRequestDecoder = null;
            try {
                postRequestDecoder = new HttpPostRequestDecoder(request);
                for (InterfaceHttpData data : postRequestDecoder.getBodyHttpDatas()) {
                    data.retain(); // must retain each attr before destroy
                    if (data.getHttpDataType() == HttpDataType.Attribute) {
                        if (data instanceof HttpData) {
                            HttpData httpData = (HttpData) data;
                            try {
                                String name = httpData.getName();
                                String value = httpData.getString();
                                serverRequest.addParam(name, value);
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
        // parse target command name.
        String target = parseTarget(queryStringDecoder.rawPath(), request.method());
        serverRequest.addMetadata(COMMAND_TARGET, target);
        // Parse body.
        if (request.content().readableBytes() <= 0) {
            serverRequest.setBody(null);
        } else {
            byte[] body = new byte[request.content().readableBytes()];
            request.content().getBytes(0, body);
            serverRequest.setBody(body);
        }
        return serverRequest;
    }

    private String parseTarget(String uri, HttpMethod method) {
        if (StringUtils.isEmpty(uri)) {
            return String.format("-%s", method);
        }
        return String.format("%s-%s", uri, method);
    }
}
