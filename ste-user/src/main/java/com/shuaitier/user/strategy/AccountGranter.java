package com.shuaitier.user.strategy;

import com.shuaiti.common.domain.dto.LoginUserDTO;
import com.shuaitier.user.domain.vo.LoginUserVO;
import com.shuaitier.user.enums.LoginType;
import org.springframework.stereotype.Component;

@Component
public class AccountGranter implements AuthGranter {
    @Override
    public String loginType() {
        return LoginType.ACCOUNT.name();
    }

    @Override
    public LoginUserVO login(LoginUserDTO loginUserDTO) {
        throw new IllegalArgumentException("当前版本仅支持手机号登录和微信登录");
    }
}
