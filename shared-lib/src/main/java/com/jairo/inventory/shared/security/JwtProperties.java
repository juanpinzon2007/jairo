package com.jairo.inventory.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String secret,
                            long accessTokenMinutes,
                            String issuer) {
}
