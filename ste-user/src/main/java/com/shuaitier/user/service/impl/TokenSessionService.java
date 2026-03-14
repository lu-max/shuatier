package com.shuaitier.user.service.impl;

import com.shuaiti.common.constants.SecurityConstants;
import com.shuaiti.common.domain.dto.JwtUserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenSessionService {
    private final StringRedisTemplate stringRedisTemplate;

    public void saveAccessToken(String accessToken, String refreshToken, long expireMinutes) {
        stringRedisTemplate.opsForValue().set(
                buildKey(SecurityConstants.LOGIN_TOKEN_KEY_PREFIX, accessToken),
                refreshToken == null ? "" : refreshToken,
                Duration.ofMinutes(expireMinutes)
        );
    }

    public void saveRefreshToken(String refreshToken, JwtUserInfoDTO userInfo, long expireMinutes) {
        String value = userInfo.getUserId() + ":" + userInfo.getPhone();
        stringRedisTemplate.opsForValue().set(
                buildKey(SecurityConstants.REFRESH_TOKEN_KEY_PREFIX, refreshToken),
                value,
                Duration.ofMinutes(expireMinutes)
        );
    }

    public boolean existsRefreshToken(String token) {
        return exists(SecurityConstants.REFRESH_TOKEN_KEY_PREFIX, token);
    }

    public void removeAccessToken(String token) {
        removeToken(SecurityConstants.LOGIN_TOKEN_KEY_PREFIX, token);
    }

    public void removeRefreshToken(String token) {
        removeToken(SecurityConstants.REFRESH_TOKEN_KEY_PREFIX, token);
    }

    public String getBoundRefreshToken(String accessToken) {
        return stringRedisTemplate.opsForValue().get(buildKey(SecurityConstants.LOGIN_TOKEN_KEY_PREFIX, accessToken));
    }

    private boolean exists(String prefix, String token) {
        Boolean result = stringRedisTemplate.hasKey(buildKey(prefix, token));
        return Boolean.TRUE.equals(result);
    }

    private void removeToken(String prefix, String token) {
        stringRedisTemplate.delete(buildKey(prefix, token));
    }

    private String buildKey(String prefix, String token) {
        return prefix + token;
    }
}
