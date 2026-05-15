package com.programmer.escrow.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.wechat")
public class WechatProperties {

    private boolean mockEnabled;
    private String appId;
    private String appSecret;
    private String oauthScope;
    private String callbackUrl;
    private String qrEntryUrl;
    private String loginEntryUrl;
    private String subscribeUrl;
    private String officialAccountQrImageUrl;
    private String serverBaseUrl;
    private String officialAccessTokenKey;
    private Mock mock = new Mock();

    @Data
    public static class Mock {
        private String openid;
        private String nickname;
        private String avatarUrl;
        private boolean subscribed = true;
    }
}
