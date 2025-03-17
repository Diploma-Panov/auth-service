package com.mpanov.diploma.auth.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String publicKey;

    private Long accessTokenLifetime;

    private Long refreshTokenLifetime;

    private Long adminTokenLifetime;
}
