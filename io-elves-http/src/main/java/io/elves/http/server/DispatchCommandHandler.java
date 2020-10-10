package io.elves.http.server;


import com.google.common.base.Strings;
import io.elves.common.util.IpUtil;
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
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static io.elves.core.ElvesConstants.FAVICON_PATH;
import static io.elves.core.ElvesConstants.REQUEST_ID_KEY;
import static io.elves.core.ElvesConstants.SERVER_IP_KEY;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * Netty-based HTTP server handler for command center.
 *
 * @author leee
 */
@Slf4j
public class DispatchCommandHandler extends SimpleChannelInboundHandler<Object> {
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
        //ignore request favicon.ico
        if (FAVICON_PATH.equals(httpRequest.uri())) {
            return;
        }

        setRequestId(httpRequest);
        RequestContext request = BuildRequestContext.parseRequest(httpRequest);

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
        writeResponse(commandHandler.handle(request)
                , request
                , ctx
                , keepAlive);
    }

    private Encoder lookupEncoder(Class<?> clazz, String requestContextType) {
        if (clazz == null) {
            throw new IllegalArgumentException("Bad class metadata");
        }

        Encoder encoder = CodecContainer.getEncoder(requestContextType);
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

    private byte[] encodeResponseBody(CommandResponse response, String requestContextType) {
        byte[] body = new byte[]{};
        if (response.getCode() == 0) {
            if (response.getData() == null) {
                body = new byte[]{};
            } else {
                Encoder encoder = lookupEncoder(response.getData().getClass(), requestContextType);
                body = encoder.encode(response);
            }
        } else {
            body = response.getExceptionMessage() != null
                    ? response.getExceptionMessage().getBytes(StandardCharsets.UTF_8)
                    : body;
        }
        return body;
    }

    private void writeResponse(CommandResponse response
            , RequestContext request
            , ChannelHandlerContext ctx, boolean keepAlive) {

        byte[] body = encodeResponseBody(response, request.getContentType());

        HttpResponseStatus status = response.getCode() == 0 ? OK : BAD_REQUEST;

        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(body));

        httpResponse.headers().set("Content-Type", String.format("%s; charset=UTF-8"
                , request.getContentType()));
        httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
        if (keepAlive) {
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        } else {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
        ctx.write(httpResponse);
    }

    private void setRequestId(FullHttpRequest httpRequest) {
        MDC.put(REQUEST_ID_KEY, Strings.isNullOrEmpty(httpRequest.headers().get(REQUEST_ID_KEY))
                ? UUID.randomUUID().toString().replaceAll("-", "").toLowerCase()
                : httpRequest.headers().get(REQUEST_ID_KEY));
        MDC.put(SERVER_IP_KEY, IpUtil.getLocalIp());
    }
}
