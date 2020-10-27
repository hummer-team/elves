package io.elves.http.server.platform;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlatformFactory {
    private PlatformFactory() {

    }

    public static EventLoopGroup eventLoopGroup(int threadCount) {
        log.debug("Default Epoll support :  {}", Epoll.isAvailable());
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup(threadCount);
        }

        return new NioEventLoopGroup(threadCount);
    }


    public static Class<? extends ServerChannel> channel() {
        if (Epoll.isAvailable()) {
            return EpollServerSocketChannel.class;
        }

        return NioServerSocketChannel.class;
    }
}
