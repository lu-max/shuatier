package com.shuaitier.user.controller;

import com.shuaiti.common.constants.SecurityConstants;
import com.shuaiti.common.domain.Result;
import com.shuaiti.common.domain.dto.LoginUserDTO;
import com.shuaitier.user.domain.dto.RefreshTokenDTO;
import com.shuaitier.user.domain.vo.LoginUserVO;
import com.shuaitier.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginUserVO> login(@RequestBody LoginUserDTO loginUserDTO) {
        log.info("登录操作，登录方式为{}",loginUserDTO.getLoginType());
        try {
            return Result.success(authService.login(loginUserDTO));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public Result<LoginUserVO> refresh(@RequestBody RefreshTokenDTO refreshTokenDTO) {
        log.info("刷新token操作");
        try {
            return Result.success(authService.refreshToken(refreshTokenDTO));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage(), 401);
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout(
            @RequestHeader(value = SecurityConstants.AUTHORIZATION_HEADER, required = false) String authorization
    ) {
        log.info("执行退出账号操作");
        try {
            authService.logout(authorization);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage(), 401);
        }
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me(
            @RequestHeader(value = SecurityConstants.USER_ID_HEADER, required = false) Long userId,
            @RequestHeader(value = SecurityConstants.USER_PHONE_HEADER, required = false) String phone,
            @RequestHeader(value = SecurityConstants.LOGIN_TYPE_HEADER, required = false) String loginType
    ) {
        if (userId == null) {
            return Result.error("未获取到登录用户", 401);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", userId);
        data.put("phone", phone);
        data.put("loginType", loginType);
        return Result.success(data);
    }
}
