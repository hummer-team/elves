package io.elves.http.server.init;

import io.elves.http.server.handler.htt2.Http2OrHttpHandler;
import io.elves.http.server.handler.http1.DispatchCommandHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author lee
 */
public class Http2ServerInitializer extends ChannelInitializer<SocketChannel> {
    private final SslContext sslCtx;

    public Http2ServerInitializer(final SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline p = socketChannel.pipeline();
        p.addLast(new HttpRequestDecoder());
        p.addLast(new HttpObjectAggregator(1024 * 1024 * 3));
        p.addLast(new HttpResponseEncoder());
        p.addLast(new ChunkedWriteHandler());
        p.addLast(sslCtx.newHandler(socketChannel.alloc()), new Http2OrHttpHandler());
        p.addLast(new DispatchCommandHandler());
    }
}
