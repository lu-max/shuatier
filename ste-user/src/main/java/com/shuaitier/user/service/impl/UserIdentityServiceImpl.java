package com.shuaitier.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuaiti.common.constants.RegexConstants;
import com.shuaiti.common.constants.SecurityConstants;
import com.shuaiti.common.domain.dto.JwtUserInfoDTO;
import com.shuaiti.common.domain.dto.LoginUserDTO;
import com.shuaiti.common.utils.JwtUtils;
import com.shuaitier.user.config.JwtProperties;
import com.shuaitier.user.domain.entity.UserProfile;
import com.shuaitier.user.domain.vo.LoginUserVO;
import com.shuaitier.user.enums.LoginType;
import com.shuaitier.user.mapper.UserProfileMapper;
import com.shuaitier.user.service.UserIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserIdentityServiceImpl implements UserIdentityService {
    private static final int ENABLED = 1;
    private static final int NOT_DELETED = 0;
    private static final Pattern PHONE_REGEX = Pattern.compile(RegexConstants.PHONE_PATTERN);

    private final UserProfileMapper userProfileMapper;
    private final JwtProperties jwtProperties;
    private final TokenSessionService tokenSessionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginUserVO loginByPhone(LoginUserDTO loginUserDTO) {
        String phone = normalizePhone(loginUserDTO.getPhone());
        UserProfile profile = findProfileByPhone(phone);
        boolean newUser = false;
        LocalDateTime now = LocalDateTime.now();
        if (profile == null) {
            profile = new UserProfile();
            profile.setPhone(phone);
            profile.setNickname(defaultNickname(loginUserDTO.getNickname(), phone));
            profile.setAvatar(trimToNull(loginUserDTO.getAvatar()));
            profile.setSource("PHONE");
            profile.setStatus(ENABLED);
            profile.setDeleted(NOT_DELETED);
            profile.setLastLoginTime(now);
            userProfileMapper.insert(profile);
            newUser = true;
        } else {
            assertProfileAvailable(profile);
            syncProfile(profile, loginUserDTO, now);
            userProfileMapper.updateById(profile);
        }
        return buildLoginUserVO(profile, LoginType.SMS.name(), newUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginUserVO loginByWechat(LoginUserDTO loginUserDTO) {
        String phone = normalizePhone(loginUserDTO.getPhone());
        String openId = trimToNull(loginUserDTO.getOpenId());
        String unionId = trimToNull(loginUserDTO.getUnionId());
        if (!StringUtils.hasText(openId) && !StringUtils.hasText(unionId)) {
            throw new IllegalArgumentException("微信登录必须传入 openId 或 unionId");
        }

        UserProfile phoneProfile = findProfileByPhone(phone);
        UserProfile wechatProfile = findProfileByWechat(openId, unionId);
        if (phoneProfile != null && wechatProfile != null && !Objects.equals(phoneProfile.getId(), wechatProfile.getId())) {
            throw new IllegalArgumentException("当前微信信息已绑定其他手机号，请联系客服");
        }

        boolean newUser = false;
        LocalDateTime now = LocalDateTime.now();
        UserProfile profile = phoneProfile != null ? phoneProfile : wechatProfile;
        if (profile == null) {
            profile = new UserProfile();
            profile.setPhone(phone);
            profile.setNickname(defaultNickname(loginUserDTO.getNickname(), phone));
            profile.setAvatar(trimToNull(loginUserDTO.getAvatar()));
            profile.setOpenId(openId);
            profile.setUnionId(unionId);
            profile.setSource("WECHAT");
            profile.setStatus(ENABLED);
            profile.setDeleted(NOT_DELETED);
            profile.setLastLoginTime(now);
            userProfileMapper.insert(profile);
            newUser = true;
        } else {
            assertProfileAvailable(profile);
            profile.setPhone(phone);
            if (StringUtils.hasText(openId)) {
                profile.setOpenId(openId);
            }
            if (StringUtils.hasText(unionId)) {
                profile.setUnionId(unionId);
            }
            syncProfile(profile, loginUserDTO, now);
            userProfileMapper.updateById(profile);
        }
        return buildLoginUserVO(profile, LoginType.WECHAT.name(), newUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginUserVO refreshToken(String refreshToken) {
        JwtUserInfoDTO jwtUserInfo = JwtUtils.parseToken(jwtProperties.getSecret(), refreshToken);
        if (!SecurityConstants.REFRESH_TOKEN_USE.equals(jwtUserInfo.getTokenUse())) {
            throw new IllegalArgumentException("refreshToken 类型不正确");
        }
        if (!tokenSessionService.existsRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("refreshToken 已失效，请重新登录");
        }
        UserProfile profile = getRequiredProfile(jwtUserInfo.getUserId());
        tokenSessionService.removeRefreshToken(refreshToken);
        return buildLoginUserVO(profile, jwtUserInfo.getLoginType(), false);
    }

    private UserProfile getRequiredProfile(Long userId) {
        UserProfile profile = userProfileMapper.selectById(userId);
        if (profile == null) {
            throw new IllegalArgumentException("用户数据不存在");
        }
        assertProfileAvailable(profile);
        return profile;
    }

    private UserProfile findProfileByPhone(String phone) {
        return userProfileMapper.selectOne(
                Wrappers.<UserProfile>lambdaQuery()
                        .eq(UserProfile::getPhone, phone)
        );
    }

    private UserProfile findProfileByWechat(String openId, String unionId) {
        LambdaQueryWrapper<UserProfile> wrapper = Wrappers.lambdaQuery(UserProfile.class);
        boolean hasCondition = false;
        if (StringUtils.hasText(openId)) {
            wrapper.eq(UserProfile::getOpenId, openId);
            hasCondition = true;
        }
        if (StringUtils.hasText(unionId)) {
            if (hasCondition) {
                wrapper.or();
            }
            wrapper.eq(UserProfile::getUnionId, unionId);
            hasCondition = true;
        }
        if (!hasCondition) {
            return null;
        }
        return userProfileMapper.selectOne(wrapper);
    }

    private void syncProfile(UserProfile profile, LoginUserDTO loginUserDTO, LocalDateTime now) {
        if (StringUtils.hasText(loginUserDTO.getNickname())) {
            profile.setNickname(loginUserDTO.getNickname().trim());
        } else if (!StringUtils.hasText(profile.getNickname())) {
            profile.setNickname(defaultNickname(null, profile.getPhone()));
        }
        if (StringUtils.hasText(loginUserDTO.getAvatar())) {
            profile.setAvatar(loginUserDTO.getAvatar().trim());
        }
        profile.setLastLoginTime(now);
    }

    private void assertProfileAvailable(UserProfile profile) {
        if (!Objects.equals(profile.getDeleted(), NOT_DELETED)) {
            throw new IllegalArgumentException("当前手机号对应的用户已被删除");
        }
        if (!Objects.equals(profile.getStatus(), ENABLED)) {
            throw new IllegalArgumentException("当前手机号对应的用户已被禁用");
        }
    }

    private String normalizePhone(String phone) {
        String value = trimToNull(phone);
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (!PHONE_REGEX.matcher(value).matches()) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        return value;
    }

    private String defaultNickname(String nickname, String phone) {
        if (StringUtils.hasText(nickname)) {
            return nickname.trim();
        }
        String suffix = phone.length() > 4 ? phone.substring(phone.length() - 4) : phone;
        return "用户" + suffix;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private LoginUserVO buildLoginUserVO(UserProfile profile, String loginType, boolean newUser) {
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setUserId(profile.getId());
        loginUserVO.setUsername(profile.getPhone());
        loginUserVO.setPhone(profile.getPhone());
        loginUserVO.setNickname(profile.getNickname());
        loginUserVO.setLoginType(loginType);
        loginUserVO.setNewUser(newUser);

        JwtUserInfoDTO accessUserInfo = new JwtUserInfoDTO();
        accessUserInfo.setUserId(profile.getId());
        accessUserInfo.setPhone(profile.getPhone());
        accessUserInfo.setNickname(profile.getNickname());
        accessUserInfo.setLoginType(loginType);
        accessUserInfo.setTokenUse(SecurityConstants.ACCESS_TOKEN_USE);

        String accessToken = JwtUtils.createToken(
                jwtProperties.getSecret(),
                jwtProperties.getExpireMinutes(),
                jwtProperties.getIssuer(),
                accessUserInfo
        );

        JwtUserInfoDTO refreshUserInfo = new JwtUserInfoDTO();
        refreshUserInfo.setUserId(profile.getId());
        refreshUserInfo.setPhone(profile.getPhone());
        refreshUserInfo.setNickname(profile.getNickname());
        refreshUserInfo.setLoginType(loginType);
        refreshUserInfo.setTokenUse(SecurityConstants.REFRESH_TOKEN_USE);

        String refreshToken = JwtUtils.createToken(
                jwtProperties.getSecret(),
                jwtProperties.getRefreshExpireMinutes(),
                jwtProperties.getIssuer(),
                refreshUserInfo
        );

        loginUserVO.setAccessToken(accessToken);
        loginUserVO.setRefreshToken(refreshToken);
        tokenSessionService.saveAccessToken(accessToken, refreshToken, jwtProperties.getExpireMinutes());
        tokenSessionService.saveRefreshToken(refreshToken, refreshUserInfo, jwtProperties.getRefreshExpireMinutes());
        loginUserVO.setTokenType(SecurityConstants.TOKEN_TYPE);
        loginUserVO.setExpiresIn(jwtProperties.getExpireMinutes() * 60);
        loginUserVO.setRefreshExpiresIn(jwtProperties.getRefreshExpireMinutes() * 60);
        return loginUserVO;
    }
}
