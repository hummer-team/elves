package io.elves.http.server;

import io.elves.core.ElvesProperties;
import io.elves.core.ElvesServer;
import io.elves.core.command.CommandHandlerContainer;
import io.elves.http.server.handler.Http2OrHttpConfigure;
import io.elves.http.server.handler.HttpHandler;
import io.elves.http.server.ssl.SslBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

/**
 * @author lee
 */
@Slf4j
public class HttpServer implements ElvesServer {
    private Channel channel;

    @Override
    public void init() {
        CommandHandlerContainer.getInstance().registerHandle();
    }

    @Override
    public void start(String[] args) throws InterruptedException, CertificateException, SSLException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(ElvesProperties.getIoThread());
        EventLoopGroup workerGroup = new NioEventLoopGroup(ElvesProperties.getWorkThread());

        try {
            final SslContext sslContext = SslBuilder.configureTLSIfEnable();
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            if (sslContext != null) {
                                ch.pipeline().addLast(
                                        sslContext.newHandler(ch.alloc())
                                        , new Http2OrHttpConfigure());
                            } else {
                                ch.pipeline().addLast(new HttpRequestDecoder())
                                .addLast(new HttpObjectAggregator(ElvesProperties.maxRequestContentSize()))
                                .addLast(new HttpResponseEncoder())
                                .addLast(new ChunkedWriteHandler())
                                .addLast(new HttpHandler());
                                log.debug("http1 configuration done.");
                            }
                        }
                    });
            channel = b.bind(ElvesProperties.getPort()).sync().channel();
            log.debug("elves http server start success,bind port at {}", ElvesProperties.getPort());
            channel.closeFuture().sync();
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
