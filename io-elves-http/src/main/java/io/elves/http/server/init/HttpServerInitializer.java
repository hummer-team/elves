package io.elves.http.server.init;

import io.elves.http.server.handler.http1.DispatchCommandHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author lee
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline p = socketChannel.pipeline();
        p.addLast(new HttpRequestDecoder());
        p.addLast(new HttpObjectAggregator(1024 * 1024 * 3));
        p.addLast(new HttpResponseEncoder());
        p.addLast(new ChunkedWriteHandler());
        p.addLast(new DispatchCommandHandler());
    }
}
