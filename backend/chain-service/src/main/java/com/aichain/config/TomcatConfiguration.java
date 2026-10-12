package com.aichain.config;

import org.apache.coyote.http11.Http11Nio2Protocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * 使用 Windows NIO2 连接器，兼容 JDK 21 并避免旧版 Tomcat 的 Selector
 * 在部分 Windows 环境中创建 loopback pipe 失败。
 */
@Configuration
public class TomcatConfiguration implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.setProtocol(Http11Nio2Protocol.class.getName());
    }
}
