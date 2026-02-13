package kz.legeal.ease.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.jwt")
public class JwtProperties {

    private String accessSecret;

    private String refreshSecret;

    private String issuer;

    private int accessExpMin;

    private int refreshExpMin;

}