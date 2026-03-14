package com.shuaitier.user.factory;

import com.shuaiti.common.domain.dto.LoginUserDTO;
import com.shuaitier.user.strategy.AuthGranter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AuthFactory {
    private final Map<String, AuthGranter> granterMap;

    public AuthFactory(List<AuthGranter> granters) {
        this.granterMap = granters.stream()
                .collect(Collectors.toMap(AuthGranter::loginType, Function.identity()));
    }

    public AuthGranter getGranter(LoginUserDTO loginUserDTO) {
        if (loginUserDTO == null || !StringUtils.hasText(loginUserDTO.getLoginType())) {
            throw new IllegalArgumentException("loginType 不能为空");
        }
        AuthGranter granter = granterMap.get(loginUserDTO.getLoginType().toUpperCase(Locale.ROOT));
        if (granter == null) {
            throw new IllegalArgumentException("不支持的登录方式: " + loginUserDTO.getLoginType());
        }
        return granter;
    }
}
