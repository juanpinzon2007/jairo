package com.jairo.inventory.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(properties.accessTokenMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.userId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim("email", user.email())
                .claim("name", user.fullName())
                .claim("roles", user.roles())
                .signWith(key)
                .compact();
    }

    public AuthenticatedUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        return new AuthenticatedUser(
                java.util.UUID.fromString(claims.getSubject()),
                claims.get("name", String.class),
                claims.get("email", String.class),
                roles
        );
    }
}
