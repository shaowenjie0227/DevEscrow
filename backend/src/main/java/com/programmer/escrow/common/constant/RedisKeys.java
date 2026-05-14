package com.programmer.escrow.common.constant;

public final class RedisKeys {

    public static final String AUTH_TOKEN = "auth:token:%s";
    public static final String AUTH_USER_TOKENS = "auth:user_tokens:%s";
    public static final String SESSION_USER = "session:user:%s";
    public static final String ADMIN_AUTH_TOKEN = "admin:token:%s";
    public static final String ADMIN_SESSION = "admin:session:%s";
    public static final String DEMAND_HOT_LIST = "demand:hot:list:%s";
    public static final String DEMAND_DETAIL = "demand:detail:%s";
    public static final String CHAT_RECENT = "chat:recent:%s";
    public static final String CHAT_UNREAD = "chat:unread:%s";

    private RedisKeys() {
    }
}
