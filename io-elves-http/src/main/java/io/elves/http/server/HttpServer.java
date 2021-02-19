package io.elves.http.server;

import com.google.common.base.Strings;
import io.elves.common.util.ResourceUtil;
import io.elves.core.ElvesApplication;
import io.elves.core.ElvesServer;
import io.elves.core.command.CommandActionMapping;
import io.elves.core.command.CommandConext;
import io.elves.core.command.CommandHandlerApplicationContext;
import io.elves.core.command.CommandHandlerMapping;
import io.elves.core.command.GlobalException;
import io.elves.core.command.GlobalExceptionHandler;
import io.elves.core.life.LifeApplicationContext;
import io.elves.core.life.LifeX;
import io.elves.core.log.LogRegister;
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
import java.lang.reflect.Method;
import java.security.cert.CertificateException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author lee
 */
@Slf4j
public class HttpServer implements ElvesServer {
    private Channel channel;

    @Override
    public void init(ElvesApplication application) throws Exception {
        long start = System.currentTimeMillis();
        Banner.print();
        LogRegister.init();
        ElvesProperties.load();
        registerLifeXAndExecute(application);
        //
        registerCommand(application);
        CommandHandlerApplicationContext.getInstance().registerHandle();
        log.debug("elves http server init done cost {} ms", System.currentTimeMillis() - start);
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
            log.debug("elves http server start success,enable h2 {},port at {}", ElvesProperties.enableH2()
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
            CommandHandlerApplicationContext.getInstance().destroyCommandResource();
        } catch (Throwable e) {
            //ignore
        }

        try {
            LifeApplicationContext.destroy();
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

    private void registerLifeXAndExecute(ElvesApplication application) {
        Set<Class<?>> classes = ResourceUtil.getClassesByPackageName(application.scanPackage());
        for (Class<?> cl : classes) {
            Method[] methods = cl.getMethods();
            for (Method m : methods) {
                LifeX life = m.getAnnotation(LifeX.class);
                if (life != null) {
                    try {
                        m.setAccessible(true);
                        m.invoke(ResourceUtil.tryInstance(cl));
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        LifeApplicationContext.postconstruct();
    }

    private void registerCommand(ElvesApplication application) {
        String scanPackage = application.scanPackage();
        if (Strings.isNullOrEmpty(scanPackage)) {
            throw new RuntimeException("scan package can't null");
        }

        Set<Class<?>> classes = ResourceUtil.getClassesByPackageName(scanPackage);
        for (Class<?> cl : classes) {
            //scan GlobalException handler
            if (cl.getAnnotation(GlobalException.class) != null) {
                Object exObj = ResourceUtil.tryInstance(cl);
                if (exObj instanceof GlobalExceptionHandler) {
                    CommandHandlerApplicationContext.getInstance()
                            .registerExceptionIntercept((GlobalExceptionHandler)exObj);
                }
            }
            //scan handler controller
            CommandHandlerMapping handlerMapping = cl.getAnnotation(CommandHandlerMapping.class);
            if (handlerMapping != null) {
                Method[] methods = cl.getMethods();
                for (Method m : methods) {
                    CommandActionMapping actionMapping = m.getAnnotation(CommandActionMapping.class);
                    if (actionMapping != null) {
                        String commandPath = String.format("%s-%s%s"
                                , actionMapping.httpMethod()
                                , handlerMapping.name()
                                , actionMapping.name());
                        CommandHandlerApplicationContext.getInstance().registerCommand(commandPath
                                , new CommandConext(null
                                        , actionMapping.name()
                                        , actionMapping.desc()
                                        , actionMapping.async()
                                        , actionMapping.httpMethod()
                                        , actionMapping.respEncoderType()
                                        , m
                                        , ResourceUtil.tryInstance(cl)));
                    }
                }
            }
        }
    }
}
