package io.elves.http.server.ssl;

import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

import static io.netty.handler.codec.http2.Http2SecurityUtil.CIPHERS;

public class SslBuilder {
    private SslBuilder() {

    }

    public static SslContext configureTLS() throws CertificateException, SSLException {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        ApplicationProtocolConfig apn = new ApplicationProtocolConfig(
                ApplicationProtocolConfig.Protocol.ALPN,
                // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                ApplicationProtocolNames.HTTP_2,
                ApplicationProtocolNames.HTTP_1_1);

        return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey(), null)
                .ciphers(CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .applicationProtocolConfig(apn).build();
    }
}
