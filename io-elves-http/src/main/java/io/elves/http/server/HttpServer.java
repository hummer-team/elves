package io.elves.http.server;

import io.elves.core.ElvesProperty;
import io.elves.core.ElvesServer;
import io.elves.core.coder.CodecContainer;
import io.elves.core.command.CommandHandlerContainer;
import io.elves.http.server.init.HttpServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServer implements ElvesServer {

    private Channel channel;

    @Override
    public void init() {
        CommandHandlerContainer.getInstance().registerHandle();
    }

    @Override
    public void start(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(ElvesProperty.getIoThread());
        EventLoopGroup workerGroup = new NioEventLoopGroup(ElvesProperty.getWorkThread());

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new HttpServerInitializer());

            ChannelFuture channelFuture = b.bind(ElvesProperty.getPort()).sync();
            channel = channelFuture.channel();
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    log.error("channel bind failed ", future.cause());
                }
            });
            log.info("elves http server start ok , port: {}", ElvesProperty.getPort());
            channel.closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    public void close() {
        channel.close();
        CodecContainer.clear();
        log.info("elves http server channel closed.");
    }
}
