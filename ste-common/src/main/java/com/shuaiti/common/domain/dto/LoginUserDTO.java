package com.shuaiti.common.domain.dto;

import lombok.Data;

@Data
public class LoginUserDTO {
    private String loginType;
    private String username;
    private String phone;
    private String password;
    private String verifyCode;
    private String openId;
    private String unionId;
    private String nickname;
    private String avatar;
}
