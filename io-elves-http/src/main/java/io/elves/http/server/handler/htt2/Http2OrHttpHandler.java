package io.elves.http.server.handler.htt2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapter;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapterBuilder;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;

public class Http2OrHttpHandler extends ApplicationProtocolNegotiationHandler {
    private static final int MAX_CONTENT_LENGTH = 1024 * 100;

    public Http2OrHttpHandler() {
        super(ApplicationProtocolNames.HTTP_1_1);
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            configureHttp2(ctx);
            return;
        }

        if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
            configureHttp1(ctx);
            return;
        }

        throw new IllegalStateException("unknown protocol: " + protocol);
    }

    private static void configureHttp2(ChannelHandlerContext ctx) {
        DefaultHttp2Connection connection = new DefaultHttp2Connection(true);
        InboundHttp2ToHttpAdapter listener = new InboundHttp2ToHttpAdapterBuilder(connection)
                .propagateSettings(true).validateHttpHeaders(false)
                .maxContentLength(MAX_CONTENT_LENGTH).build();

        ctx.pipeline().addLast(new HttpToHttp2ConnectionHandlerBuilder()
                .frameListener(listener)
                // .frameLogger(TilesHttp2ToHttpHandler.logger)
                .connection(connection).build());

        ctx.pipeline().addLast(new DispatchHttp2RequestHandler());
    }

    private static void configureHttp1(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().addLast(new HttpServerCodec(),
                new HttpObjectAggregator(MAX_CONTENT_LENGTH),
                new FallbackRequestHandler());
    }
}
