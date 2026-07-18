package com.financafacil.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Mail mail = new Mail();
    private Frontend frontend = new Frontend();

    @Getter @Setter
    public static class Jwt {
        private String accessSecret;
        private String refreshSecret;
        private long accessExpirationMs;
        private long refreshExpirationMs;
    }

    @Getter @Setter
    public static class Cors {
        private String allowedOrigins;
    }

    @Getter @Setter
    public static class Mail {
        private String from;
    }

    @Getter @Setter
    public static class Frontend {
        private String url;
    }
}
