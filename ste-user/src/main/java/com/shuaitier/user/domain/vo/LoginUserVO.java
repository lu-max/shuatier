package com.shuaitier.user.domain.vo;

import lombok.Data;

@Data
public class LoginUserVO {
    private Long userId;
    private String username;
    private String phone;
    private String nickname;
    private String loginType;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private Long refreshExpiresIn;
    private Boolean newUser;
}
