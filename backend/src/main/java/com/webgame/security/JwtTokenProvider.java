package com.webgame.security;

import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    @Value("${webgame.jwt.secret}")
    private String secret;

    @Value("${webgame.jwt.issuer}")
    private String issuer;

    @Value("${webgame.jwt.access-token-expire-seconds}")
    private long accessTokenExpireSeconds;

    @Value("${webgame.jwt.refresh-token-expire-seconds}")
    private long refreshTokenExpireSeconds;

    public String generateAccessToken(Long userId, String username) {
        return buildToken(userId, username, accessTokenExpireSeconds, "access");
    }

    public String generateRefreshToken(Long userId, String username) {
        return buildToken(userId, username, refreshTokenExpireSeconds, "refresh");
    }

    public Claims parseToken(String token) {
        return Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token).getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private String buildToken(Long userId, String username, long expireSeconds, String tokenType) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(username)
                .claim("userId", userId)
                .claim("tokenType", tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expireSeconds)))
                .signWith(getSecretKey())
                .compact();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
