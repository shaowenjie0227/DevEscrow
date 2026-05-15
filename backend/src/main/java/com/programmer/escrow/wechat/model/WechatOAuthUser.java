package com.programmer.escrow.wechat.model;

public class WechatOAuthUser {

    private final String openid;
    private final String nickname;
    private final String avatarUrl;
    private final boolean subscribed;

    public WechatOAuthUser(String openid, String nickname, String avatarUrl, boolean subscribed) {
        this.openid = openid;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.subscribed = subscribed;
    }

    public String openid() {
        return openid;
    }

    public String nickname() {
        return nickname;
    }

    public String avatarUrl() {
        return avatarUrl;
    }

    public boolean subscribed() {
        return subscribed;
    }
}
