package com.shuaitier.user.strategy;

import com.shuaiti.common.domain.dto.LoginUserDTO;
import com.shuaitier.user.domain.vo.LoginUserVO;

public interface AuthGranter {
    String loginType();

    LoginUserVO login(LoginUserDTO loginUserDTO);
}
