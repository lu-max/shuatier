package com.shuaitier.user.strategy;

import com.shuaiti.common.domain.dto.LoginUserDTO;
import com.shuaitier.user.domain.vo.LoginUserVO;
import com.shuaitier.user.enums.LoginType;
import com.shuaitier.user.service.UserIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeChatGranter implements AuthGranter {
    private final UserIdentityService userIdentityService;

    @Override
    public String loginType() {
        return LoginType.WECHAT.name();
    }

    @Override
    public LoginUserVO login(LoginUserDTO loginUserDTO) {
        return userIdentityService.loginByWechat(loginUserDTO);
    }
}
