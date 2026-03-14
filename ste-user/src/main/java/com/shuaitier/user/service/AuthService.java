package com.shuaitier.user.service;

import com.shuaiti.common.domain.dto.LoginUserDTO;
import com.shuaitier.user.domain.dto.RefreshTokenDTO;
import com.shuaitier.user.domain.vo.LoginUserVO;

public interface AuthService {
    LoginUserVO login(LoginUserDTO loginUserDTO);

    LoginUserVO refreshToken(RefreshTokenDTO refreshTokenDTO);

    void logout(String authorization);
}
