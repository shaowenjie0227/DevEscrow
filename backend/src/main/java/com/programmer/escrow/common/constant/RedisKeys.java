package com.programmer.escrow.common.constant;

public final class RedisKeys {

    public static final String LOGIN_QR = "LOGIN_QR:%s";
    public static final String LOGIN_QR_CODE = "LOGIN_QR_CODE:%s";
    public static final String LOGIN_QR_SCAN_LOCK = "LOGIN_QR_SCAN_LOCK:%s";
    public static final String LOGIN_EMAIL_CODE = "LOGIN_EMAIL_CODE:%s";
    public static final String LOGIN_EMAIL_CODE_SEND_LOCK = "LOGIN_EMAIL_CODE_SEND_LOCK:%s";
    public static final String LOGIN_USER = "LOGIN_USER:%s";
    public static final String JWT_BLACKLIST = "JWT_BLACKLIST:%s";
    public static final String WECHAT_ACCESS_TOKEN = "WECHAT:ACCESS_TOKEN";
    public static final String AUTH_TOKEN = "auth:token:%s";
    public static final String AUTH_USER_TOKENS = "auth:user_tokens:%s";
    public static final String SESSION_USER = "session:user:%s";
    public static final String ADMIN_AUTH_TOKEN = "admin:token:%s";
    public static final String ADMIN_SESSION = "admin:session:%s";
    public static final String DEMAND_HOT_LIST = "demand:hot:list:%s";
    public static final String DEMAND_DETAIL = "demand:detail:%s";
    public static final String CHAT_RECENT = "chat:recent:%s";
    public static final String CHAT_UNREAD = "chat:unread:%s";
    public static final String AI_DEMAND_DRAFT = "ai:demand:draft:%s";

    private RedisKeys() {
    }
}
