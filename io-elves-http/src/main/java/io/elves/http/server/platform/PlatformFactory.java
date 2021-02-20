package io.elves.http.server.platform;

import io.elves.core.properties.ElvesProperties;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lee
 */
@Slf4j
public class PlatformFactory {
    private static boolean epollSupport = false;
    private static boolean kqueueSupport = false;

    static {
        try {
            epollSupport = enableEpoll() && Epoll.isAvailable();
            kqueueSupport = enableKqueue() && KQueue.isAvailable();
        } catch (Throwable e) {
            //ignore
        }
    }

    private PlatformFactory() {

    }

    public static EventLoopGroup eventLoopGroup(int threadCount) {
        log.debug("Default Epoll support :  {}, kQueue support : {}"
                , epollSupport
                , kqueueSupport);
        if (epollSupport) {
            return new EpollEventLoopGroup(threadCount);
        }
        if (kqueueSupport) {
            return new KQueueEventLoopGroup(threadCount);
        }
        return new NioEventLoopGroup(threadCount);
    }

    public static Class<? extends ServerChannel> channel() {
        if (epollSupport) {
            return EpollServerSocketChannel.class;
        }
        if (kqueueSupport) {
            return KQueueServerSocketChannel.class;
        }
        return NioServerSocketChannel.class;
    }

    private static boolean enableEpoll() {
        return "true".equals(ElvesProperties.getProperties().getProperty("elves.server.platform.enable.epoll"
                , "false"));
    }

    private static boolean enableKqueue() {
        return "true".equals(ElvesProperties.getProperties().getProperty("elves.server.platform.enable.kqueue"
                , "false"));
    }
}
