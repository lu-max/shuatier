package com.shuaitier.user.service;

import com.shuaiti.common.domain.dto.LoginUserDTO;
import com.shuaitier.user.domain.vo.LoginUserVO;

public interface UserIdentityService {
    LoginUserVO loginByPhone(LoginUserDTO loginUserDTO);

    LoginUserVO loginByWechat(LoginUserDTO loginUserDTO);

    LoginUserVO refreshToken(String refreshToken);
}
