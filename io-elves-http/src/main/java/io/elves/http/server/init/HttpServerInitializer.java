package io.elves.http.server.init;

import io.elves.core.properties.ElvesProperties;
import io.elves.http.server.handler.Http2OrHttpConfigure;
import io.elves.http.server.ssl.SslBuilder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

/**
 * @author lee
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    public HttpServerInitializer() {

    }

    @Override
    protected void initChannel(SocketChannel socketChannel)
            throws CertificateException, SSLException {
        ChannelPipeline p = socketChannel.pipeline();
        if (ElvesProperties.enableH2()) {
            p.addLast(SslBuilder.configureTLSIfEnable().newHandler(socketChannel.alloc()));
        }
        p.addLast(new Http2OrHttpConfigure());
    }
}
