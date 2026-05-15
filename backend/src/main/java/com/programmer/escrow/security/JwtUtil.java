package com.programmer.escrow.security;

import com.programmer.escrow.config.properties.JwtProperties;
import com.programmer.escrow.security.model.IssuedJwtToken;
import com.programmer.escrow.security.model.JwtUserClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public IssuedJwtToken issueToken(Long userId, Integer userType, String nickname, List<String> roles) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.getAccessTokenTtlSeconds());
        String jti = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(String.valueOf(userId))
                .id(jti)
                .claim("userType", userType)
                .claim("nickname", nickname)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
        return new IssuedJwtToken(token, jti, expiresAt, roles);
    }

    public JwtUserClaims parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        List<String> roles = claims.get("roles", List.class);
        List<String> normalizedRoles = roles == null ? List.of() : roles.stream().map(String::valueOf).toList();
        Integer userType = claims.get("userType", Integer.class);
        return new JwtUserClaims(
                claims.getId(),
                Long.parseLong(claims.getSubject()),
                userType,
                claims.get("nickname", String.class),
                normalizedRoles,
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant()
        );
    }

    public boolean shouldRefresh(JwtUserClaims claims) {
        return getRemainingSeconds(claims) <= jwtProperties.getRefreshThresholdSeconds();
    }

    public long getRemainingSeconds(JwtUserClaims claims) {
        return Math.max(0L, claims.expiresAt().getEpochSecond() - Instant.now().getEpochSecond());
    }

    public long getRemainingSeconds(String token) {
        if (!StringUtils.hasText(token)) {
            return 0L;
        }
        try {
            return getRemainingSeconds(parseToken(token));
        } catch (Exception ex) {
            return 0L;
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }
}
