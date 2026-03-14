package com.shuaiti.common.constants;

public interface SecurityConstants {
    String AUTHORIZATION_HEADER = "Authorization";
    String TOKEN_PREFIX = "Bearer ";
    String TOKEN_TYPE = "Bearer";

    String ACCESS_TOKEN_USE = "ACCESS";
    String REFRESH_TOKEN_USE = "REFRESH";

    String CLAIM_USER_ID = "userId";
    String CLAIM_PHONE = "phone";
    String CLAIM_LOGIN_TYPE = "loginType";
    String CLAIM_NICKNAME = "nickname";
    String CLAIM_TOKEN_USE = "tokenUse";

    String USER_ID_HEADER = "X-User-Id";
    String USER_PHONE_HEADER = "X-User-Phone";
    String LOGIN_TYPE_HEADER = "X-Login-Type";

    String LOGIN_TOKEN_KEY_PREFIX = "auth:token:";
    String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh:";
}
