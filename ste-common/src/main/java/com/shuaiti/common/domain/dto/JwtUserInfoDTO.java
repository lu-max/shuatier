package com.shuaiti.common.domain.dto;

import lombok.Data;

@Data
public class JwtUserInfoDTO {
    private Long userId;
    private String phone;
    private String nickname;
    private String loginType;
    private String tokenUse;
}
