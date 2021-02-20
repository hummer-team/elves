package io.elves.http.server.handler;

import com.google.common.base.Strings;
import io.elves.common.exception.CommandException;
import io.elves.core.context.ResponseContext;
import io.netty.buffer.ByteBuf;
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
import io.netty.handler.codec.http2.HttpConversionUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import static io.elves.core.ElvesConstants.FAVICON_PATH;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpUtil.setContentLength;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
public class H2Handler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static String streamId(FullHttpRequest request) {
        return request.headers().get(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
    }

    private static void streamId(FullHttpResponse response, String streamId) {
        response.headers().set(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), streamId);
    }

    private static void writeErrorResponse(ChannelHandlerContext ctx
            , HttpResponseStatus status, String errorMessage, String streamId) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status
                , Unpooled.copiedBuffer(Strings.isNullOrEmpty(errorMessage)
                ? "sys error"
                : errorMessage, StandardCharsets.UTF_8));
        streamId(response, streamId);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("dispatch h2 command exception ", cause);
        ctx.close();
    }

    /**
     * Is called for each message of type {@link I}.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *            belongs to
     * @param msg the message to handle
     * @throws Exception is thrown if an error occurred
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //ignore request favicon.ico
        String streamId = streamId(request);
        if (FAVICON_PATH.equals(request.uri())) {
            writeErrorResponse(ctx, NOT_FOUND, "", streamId);
            return;
        }
        try {
            ResponseContext respContext =  DispatchV2CommandHandler.INSTANCE.dispatch(request);

            ByteBuf content = ctx.alloc().buffer(respContext.getBytes().length);
            content.writeBytes(respContext.getBytes());

            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1
                    , respContext.getStatus()
                    , content);
            response.headers().add(respContext.getHeaders());
            writeResponse(ctx, streamId, response, HttpUtil.isKeepAlive(request));

        } catch (Throwable ex) {
            log.warn("h2 Internal error", ex);
            HttpResponseStatus status = INTERNAL_SERVER_ERROR;
            if (ex instanceof CommandException) {
                status = ((CommandException) ex).getStatus();
            }
            writeErrorResponse(ctx, status, ex.getMessage(), streamId);
        }
    }

    protected void writeResponse(final ChannelHandlerContext ctx
            , final String streamId
            , final FullHttpResponse response
            , final boolean keepAlive) {
        setContentLength(response, response.content().readableBytes());
        streamId(response, streamId);
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        } else {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
        ctx.write(response);
    }
}
