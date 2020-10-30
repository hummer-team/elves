package io.elves.http.server.handler;


import com.google.common.base.Strings;
import io.elves.common.exception.CommandException;
import io.elves.core.context.ResponseConext;
import io.elves.core.properties.ElvesProperties;
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

import java.nio.charset.StandardCharsets;

import static io.elves.core.ElvesConstants.FAVICON_PATH;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * Netty-based HTTP server handler for command center.
 *
 * @author leee
 */
@Slf4j
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
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
        log.error("dispatch http command exception,close this channel. this cause is: ", cause);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        //ignore request favicon.ico
        if (FAVICON_PATH.equals(request.uri())) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, NOT_FOUND));
            return;
        }

        try {
            ResponseConext respContext = DispatchCommandHandler.INSTANCE.handler(request);
            writeResponse(respContext, ctx, HttpUtil.isKeepAlive(request));
        } catch (Throwable ex) {
            HttpResponseStatus status = INTERNAL_SERVER_ERROR;
            if (ex instanceof CommandException) {
                status = ((CommandException) ex).getStatus();
            }
            writeErrorResponse(status
                    , Strings.isNullOrEmpty(ex.getMessage())
                            ? status.toString()
                            : ex.getMessage()
                    , ctx);
            log.warn("http1 Internal error", ex);
        }
    }

    private void writeErrorResponse(HttpResponseStatus statusCode, String message, ChannelHandlerContext ctx) {
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                statusCode,
                Unpooled.copiedBuffer(message, StandardCharsets.UTF_8));

        httpResponse.headers().set("Content-Type", "text/plain; charset=UTF-8");
        if (ElvesProperties.valueOfBoolean("elves.server.exception.connection.keepalive", "true")) {
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.write(httpResponse);

        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }


    private void writeResponse(ResponseConext responseConext
            , ChannelHandlerContext ctx
            , boolean keepAlive) {

        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, OK,
                ctx.alloc().buffer(responseConext.getBytes().length).writeBytes(responseConext.getBytes()));
        httpResponse.headers().add(responseConext.getHeaders());
        httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
        if (keepAlive) {
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        } else {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
        ctx.write(httpResponse);
    }
}
