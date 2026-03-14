package com.shuaiti.gateway.config;

import com.shuaiti.common.constants.SecurityConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "shuaitier.jwt")
public class JwtProperties {
    private String secret = "shuaitier-jwt-secret-key-please-change-2026";
    private long expireMinutes = 120;
    private long refreshExpireMinutes = 10080;
    private String issuer = "shuaitier";
    private String headerName = SecurityConstants.AUTHORIZATION_HEADER;
    private String tokenPrefix = SecurityConstants.TOKEN_PREFIX;
}
