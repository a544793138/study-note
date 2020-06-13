```java
package com.mastercard.cme.caas.api.other.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.SslServerCustomizer;
import org.springframework.boot.web.server.Http2;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class NettyServerCustomizer implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    private final static String protocol = "TLSv1.2";
    private final static String storeType = "JKS";

    @Value("${server.ssl.enable:true}")
    private boolean sslEnable;

    @Override
    public void customize(NettyReactiveWebServerFactory factory) {
        final Ssl ssl = factory.getSsl();
        ssl.setEnabled(sslEnable);
        if (sslEnable) {
//            ssl.setClientAuth(Ssl.ClientAuth.NEED);
            ssl.setProtocol(protocol);
            ssl.setKeyStoreType(storeType);
            ssl.setTrustStoreType(storeType);
            Http2 http2 = new Http2();
            http2.setEnabled(false);
            factory.addServerCustomizers(new SslServerCustomizer(ssl, http2, null));
        }
    }
}
```