package com.shuaiti.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuaiti.common.constants.SecurityConstants;
import com.shuaiti.common.domain.Result;
import com.shuaiti.common.domain.dto.JwtUserInfoDTO;
import com.shuaiti.common.utils.JwtUtils;
import com.shuaiti.gateway.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    private static final List<String> WHITE_LIST = List.of(
            "/api/user/auth/login",
            "/api/user/auth/refresh",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/favicon.ico",
            "/error"
    );

    private final ObjectMapper objectMapper;
    private final JwtProperties jwtProperties;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod()) || isWhiteListed(path)) {
            return chain.filter(exchange);
        }
        String authorization = exchange.getRequest().getHeaders().getFirst(jwtProperties.getHeaderName());
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(jwtProperties.getTokenPrefix())) {
            return unauthorized(exchange, "未登录，请先登录");
        }

        String token = authorization.substring(jwtProperties.getTokenPrefix().length()).trim();
        try {
            JwtUserInfoDTO userInfo = JwtUtils.parseToken(jwtProperties.getSecret(), token);
            if (userInfo.getUserId() == null) {
                return unauthorized(exchange, "登录信息不完整");
            }
            if (!SecurityConstants.ACCESS_TOKEN_USE.equals(userInfo.getTokenUse())) {
                return unauthorized(exchange, "当前令牌不能访问业务接口");
            }
            String redisKey = SecurityConstants.LOGIN_TOKEN_KEY_PREFIX + token;
            return redisTemplate.hasKey(redisKey).flatMap(exists -> {
                if (!Boolean.TRUE.equals(exists)) {
                    return unauthorized(exchange, "登录已退出，请重新登录");
                }
                return redisTemplate.expire(redisKey, Duration.ofMinutes(jwtProperties.getExpireMinutes()))
                        .then(Mono.defer(() -> {
                            ServerHttpRequest request = exchange.getRequest().mutate()
                                    .header(SecurityConstants.USER_ID_HEADER, String.valueOf(userInfo.getUserId()))
                                    .header(SecurityConstants.USER_PHONE_HEADER, safeHeaderValue(userInfo.getPhone()))
                                    .header(SecurityConstants.LOGIN_TYPE_HEADER, safeHeaderValue(userInfo.getLoginType()))
                                    .build();
                            return chain.filter(exchange.mutate().request(request).build());
                        }));
            });
        } catch (Exception e) {
            return unauthorized(exchange, "登录已失效，请重新登录");
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isWhiteListed(String path) {
        for (String pattern : WHITE_LIST) {
            if (antPathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(Result.error(message, 401));
        } catch (Exception e) {
            body = ("{\"code\":401,\"msg\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        }
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }

    private String safeHeaderValue(String value) {
        return value == null ? "" : value;
    }
}
