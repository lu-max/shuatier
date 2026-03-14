package com.shuaiti.common.utils;

import com.shuaiti.common.constants.SecurityConstants;
import com.shuaiti.common.domain.dto.JwtUserInfoDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public final class JwtUtils {
    private static final int MIN_SECRET_BYTES = 32;

    private JwtUtils() {
    }

    public static String createToken(String secret, long expireMinutes, String issuer, JwtUserInfoDTO userInfo) {
        return createToken(secret, expireMinutes, issuer, userInfo, userInfo == null ? null : userInfo.getTokenUse());
    }

    public static String createToken(String secret, long expireMinutes, String issuer, JwtUserInfoDTO userInfo, String tokenUse) {
        if (userInfo == null || userInfo.getUserId() == null) {
            throw new IllegalArgumentException("JWT 用户信息不能为空");
        }
        if (expireMinutes <= 0) {
            throw new IllegalArgumentException("JWT 过期时间必须大于0");
        }
        SecretKey key = buildKey(secret);
        Instant now = Instant.now();
        Instant expireAt = now.plus(Duration.ofMinutes(expireMinutes));
        return Jwts.builder()
                .subject(String.valueOf(userInfo.getUserId()))
                .issuer(isBlank(issuer) ? "ste" : issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .claim(SecurityConstants.CLAIM_USER_ID, userInfo.getUserId())
                .claim(SecurityConstants.CLAIM_PHONE, userInfo.getPhone())
                .claim(SecurityConstants.CLAIM_LOGIN_TYPE, userInfo.getLoginType())
                .claim(SecurityConstants.CLAIM_NICKNAME, userInfo.getNickname())
                .claim(SecurityConstants.CLAIM_TOKEN_USE, tokenUse)
                .signWith(key)
                .compact();
    }

    public static JwtUserInfoDTO parseToken(String secret, String token) {
        if (isBlank(token)) {
            throw new IllegalArgumentException("JWT token 不能为空");
        }
        Claims claims = Jwts.parser()
                .verifyWith(buildKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        JwtUserInfoDTO userInfo = new JwtUserInfoDTO();
        Long userId = parseLong(claims.get(SecurityConstants.CLAIM_USER_ID));
        if (userId == null && !isBlank(claims.getSubject())) {
            userId = Long.parseLong(claims.getSubject());
        }
        userInfo.setUserId(userId);
        userInfo.setPhone(parseString(claims.get(SecurityConstants.CLAIM_PHONE)));
        userInfo.setNickname(parseString(claims.get(SecurityConstants.CLAIM_NICKNAME)));
        userInfo.setLoginType(parseString(claims.get(SecurityConstants.CLAIM_LOGIN_TYPE)));
        userInfo.setTokenUse(parseString(claims.get(SecurityConstants.CLAIM_TOKEN_USE)));
        return userInfo;
    }

    private static SecretKey buildKey(String secret) {
        byte[] keyBytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalArgumentException("JWT 密钥长度不能少于32字节");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static String parseString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
