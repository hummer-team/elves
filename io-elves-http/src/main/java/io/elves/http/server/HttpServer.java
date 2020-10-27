package io.elves.http.server;

import io.elves.core.ElvesConstants;
import io.elves.core.ElvesServer;
import io.elves.core.command.CommandHandlerContainer;
import io.elves.core.properties.ElvesProperties;
import io.elves.http.server.banner.Banner;
import io.elves.http.server.handler.HeartBeatServerHandler;
import io.elves.http.server.handler.Http2OrHttpConfigure;
import io.elves.http.server.handler.HttpHandler;
import io.elves.http.server.platform.PlatformFactory;
import io.elves.http.server.ssl.SslBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

/**
 * @author lee
 */
@Slf4j
public class HttpServer implements ElvesServer {
    private Channel channel;

    @Override
    public void init() throws IOException {
        Banner.print();
        ElvesProperties.load(System.getProperty(ElvesConstants.PROFILES_ACTIVE));
        CommandHandlerContainer.getInstance().registerHandle();
    }

    @Override
    public void start(String[] args) throws InterruptedException, CertificateException, SSLException {
        EventLoopGroup bossGroup = PlatformFactory.eventLoopGroup(ElvesProperties.getIoThread());
        EventLoopGroup workerGroup = PlatformFactory.eventLoopGroup(ElvesProperties.getWorkThread());

        final SslContext sslContext = SslBuilder.configureTLSIfEnable();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(PlatformFactory.channel())
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_RCVBUF, (int) ElvesProperties.getBufferSize())
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            if (sslContext != null) {
                                ch.pipeline()
                                        .addLast(sslContext.newHandler(ch.alloc()), new Http2OrHttpConfigure())
                                        .addFirst(new IdleStateHandler(
                                                ElvesProperties.getIdleReadTimeOutSecond()
                                                , ElvesProperties.getIdleWriteTimeoutSecond()
                                                , 0
                                                , TimeUnit.SECONDS))
                                        .addLast(new HeartBeatServerHandler());
                                ;
                            } else {
                                ch.pipeline()
                                        .addLast(new HttpRequestDecoder())
                                        .addLast(new HttpObjectAggregator(ElvesProperties.maxRequestContentSize()))
                                        .addLast(new HttpResponseEncoder())
                                        .addLast(new ChunkedWriteHandler())
                                        .addLast(new HttpHandler())
                                        .addFirst(new IdleStateHandler(
                                                ElvesProperties.getIdleReadTimeOutSecond()
                                                , ElvesProperties.getIdleWriteTimeoutSecond()
                                                , 0
                                                , TimeUnit.SECONDS))
                                        .addLast(new HeartBeatServerHandler());
                                log.debug("http1 configuration done.");
                            }
                        }
                    });
            channel = b.bind(ElvesProperties.getPort()).sync().channel();
            log.debug("elves http server start success,enable h2 {},bind port at {}", ElvesProperties.enableH2()
                    , ElvesProperties.getPort());
            channel.closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            close();
        }
    }

    @Override
    public void close() {
        try {
            CommandHandlerContainer.getInstance().destroyCommandResource();
        } catch (Throwable e) {
            //ignore
        }
        if (channel != null) {
            try {
                channel.close();
            } catch (Throwable e) {
                //ignore
            }
        }
        log.debug("elves http server closed.");
    }
}
