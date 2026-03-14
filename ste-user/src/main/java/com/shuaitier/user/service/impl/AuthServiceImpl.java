package com.shuaitier.user.service.impl;

import com.shuaiti.common.constants.SecurityConstants;
import com.shuaiti.common.domain.dto.LoginUserDTO;
import com.shuaitier.user.domain.dto.RefreshTokenDTO;
import com.shuaitier.user.domain.vo.LoginUserVO;
import com.shuaitier.user.factory.AuthFactory;
import com.shuaitier.user.service.AuthService;
import com.shuaitier.user.service.UserIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthFactory authFactory;
    private final UserIdentityService userIdentityService;
    private final TokenSessionService tokenSessionService;

    @Override
    public LoginUserVO login(LoginUserDTO loginUserDTO) {
        return authFactory.getGranter(loginUserDTO).login(loginUserDTO);
    }

    @Override
    public LoginUserVO refreshToken(RefreshTokenDTO refreshTokenDTO) {
        if (refreshTokenDTO == null || !StringUtils.hasText(refreshTokenDTO.getRefreshToken())) {
            throw new IllegalArgumentException("refreshToken 不能为空");
        }
        return userIdentityService.refreshToken(refreshTokenDTO.getRefreshToken().trim());
    }

    @Override
    public void logout(String authorization) {
        String accessToken = resolveAccessToken(authorization);
        String refreshToken = tokenSessionService.getBoundRefreshToken(accessToken);
        tokenSessionService.removeAccessToken(accessToken);
        if (StringUtils.hasText(refreshToken)) {
            tokenSessionService.removeRefreshToken(refreshToken.trim());
        }
    }

    private String resolveAccessToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            throw new IllegalArgumentException("请求头中缺少有效的 Authorization");
        }
        String token = authorization.substring(SecurityConstants.TOKEN_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("token 不能为空");
        }
        return token;
    }
}
