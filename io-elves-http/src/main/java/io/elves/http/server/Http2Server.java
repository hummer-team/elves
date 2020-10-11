package io.elves.http.server;

import io.elves.core.ElvesProperty;
import io.elves.core.ElvesServer;
import io.elves.core.command.CommandHandlerContainer;
import io.elves.http.server.init.Http2ServerInitializer;
import io.elves.http.server.ssl.SslBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

@Slf4j
public class Http2Server implements ElvesServer {
    private Channel channel;

    @Override
    public void init() {
        CommandHandlerContainer.getInstance().registerHandle();
    }

    @Override
    public void start(String[] args) throws InterruptedException, CertificateException, SSLException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(ElvesProperty.getIoThread());
        EventLoopGroup workerGroup = new NioEventLoopGroup(ElvesProperty.getWorkThread());

        final SslContext sslCtx = SslBuilder.configureTLS();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new Http2ServerInitializer(sslCtx));
            Channel ch = b.bind(ElvesProperty.getSslPort()).sync().channel();
            ch.closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    public void close() {
        channel.close();
    }
}
