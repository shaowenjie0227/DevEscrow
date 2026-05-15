package com.programmer.escrow.wechat.service;

import com.programmer.escrow.common.constant.RedisKeys;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.config.properties.WechatProperties;
import com.programmer.escrow.wechat.model.WechatOAuthTokenResponse;
import com.programmer.escrow.wechat.model.WechatOAuthUser;
import com.programmer.escrow.wechat.model.WechatOfficialAccessTokenResponse;
import com.programmer.escrow.wechat.model.WechatUserInfoResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Service
public class WechatOfficialAccountService {

    private final WechatProperties wechatProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final RestClient restClient;

    public WechatOfficialAccountService(WechatProperties wechatProperties,
                                        StringRedisTemplate stringRedisTemplate,
                                        RestClient.Builder restClientBuilder) {
        this.wechatProperties = wechatProperties;
        this.stringRedisTemplate = stringRedisTemplate;
        this.restClient = restClientBuilder.build();
    }

    public String buildAuthorizeUrl(String state) {
        if (wechatProperties.isMockEnabled()) {
            return wechatProperties.getServerBaseUrl() + "/wx/mock/authorize?state=" +
                    URLEncoder.encode(state, StandardCharsets.UTF_8);
        }

        String redirectUri = URLEncoder.encode(wechatProperties.getCallbackUrl(), StandardCharsets.UTF_8);
        return "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
                + wechatProperties.getAppId()
                + "&redirect_uri=" + redirectUri
                + "&response_type=code&scope=" + wechatProperties.getOauthScope()
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8)
                + "#wechat_redirect";
    }

    public WechatOAuthUser authorize(String code) {
        if (wechatProperties.isMockEnabled()) {
            return resolveMockUser(code);
        }

        WechatOAuthTokenResponse oauthToken = restClient.get()
                .uri(UriComponentsBuilder.fromHttpUrl("https://api.weixin.qq.com/sns/oauth2/access_token")
                        .queryParam("appid", wechatProperties.getAppId())
                        .queryParam("secret", wechatProperties.getAppSecret())
                        .queryParam("code", code)
                        .queryParam("grant_type", "authorization_code")
                        .toUriString())
                .retrieve()
                .body(WechatOAuthTokenResponse.class);
        validateWechatResponse(oauthToken == null ? null : oauthToken.getErrcode(),
                oauthToken == null ? "wechat oauth token response is empty" : oauthToken.getErrmsg());
        if (oauthToken == null || !StringUtils.hasText(oauthToken.getOpenid())) {
            throw new BizException(502, "wechat oauth openid is empty");
        }

        WechatUserInfoResponse officialUser = fetchOfficialUserInfo(oauthToken.getOpenid());
        boolean subscribed = officialUser != null && Integer.valueOf(1).equals(officialUser.getSubscribe());
        String nickname = officialUser == null ? null : officialUser.getNickname();
        String avatarUrl = officialUser == null ? null : officialUser.getHeadimgurl();

        if (!StringUtils.hasText(nickname) || !StringUtils.hasText(avatarUrl)) {
            WechatUserInfoResponse oauthUserInfo = restClient.get()
                    .uri(UriComponentsBuilder.fromHttpUrl("https://api.weixin.qq.com/sns/userinfo")
                            .queryParam("access_token", oauthToken.getAccessToken())
                            .queryParam("openid", oauthToken.getOpenid())
                            .queryParam("lang", "zh_CN")
                            .toUriString())
                    .retrieve()
                    .body(WechatUserInfoResponse.class);
            validateWechatResponse(oauthUserInfo == null ? null : oauthUserInfo.getErrcode(),
                    oauthUserInfo == null ? null : oauthUserInfo.getErrmsg());
            if (oauthUserInfo != null) {
                if (!StringUtils.hasText(nickname)) {
                    nickname = oauthUserInfo.getNickname();
                }
                if (!StringUtils.hasText(avatarUrl)) {
                    avatarUrl = oauthUserInfo.getHeadimgurl();
                }
            }
        }

        return new WechatOAuthUser(oauthToken.getOpenid(), nickname, avatarUrl, subscribed);
    }

    public String buildFollowUrl(String token) {
        String separator = wechatProperties.getSubscribeUrl().contains("?") ? "&" : "?";
        return wechatProperties.getSubscribeUrl() + separator + "state=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private WechatOAuthUser resolveMockUser(String code) {
        String[] parts = code.split("\\|");
        String openid = parts.length > 1 ? parts[1] : wechatProperties.getMock().getOpenid();
        boolean subscribed = parts.length > 2 ? "1".equals(parts[2]) : wechatProperties.getMock().isSubscribed();
        return new WechatOAuthUser(
                openid,
                wechatProperties.getMock().getNickname(),
                wechatProperties.getMock().getAvatarUrl(),
                subscribed
        );
    }

    private WechatUserInfoResponse fetchOfficialUserInfo(String openid) {
        String officialToken = fetchOfficialAccessToken();
        WechatUserInfoResponse officialUser = restClient.get()
                .uri(UriComponentsBuilder.fromHttpUrl("https://api.weixin.qq.com/cgi-bin/user/info")
                        .queryParam("access_token", officialToken)
                        .queryParam("openid", openid)
                        .queryParam("lang", "zh_CN")
                        .toUriString())
                .retrieve()
                .body(WechatUserInfoResponse.class);
        validateWechatResponse(officialUser == null ? null : officialUser.getErrcode(),
                officialUser == null ? null : officialUser.getErrmsg());
        return officialUser;
    }

    private String fetchOfficialAccessToken() {
        String redisKey = StringUtils.hasText(wechatProperties.getOfficialAccessTokenKey())
                ? wechatProperties.getOfficialAccessTokenKey()
                : RedisKeys.WECHAT_ACCESS_TOKEN;
        String cached = stringRedisTemplate.opsForValue().get(redisKey);
        if (StringUtils.hasText(cached)) {
            return cached;
        }

        WechatOfficialAccessTokenResponse response = restClient.get()
                .uri(UriComponentsBuilder.fromHttpUrl("https://api.weixin.qq.com/cgi-bin/token")
                        .queryParam("grant_type", "client_credential")
                        .queryParam("appid", wechatProperties.getAppId())
                        .queryParam("secret", wechatProperties.getAppSecret())
                        .toUriString())
                .retrieve()
                .body(WechatOfficialAccessTokenResponse.class);
        validateWechatResponse(response == null ? null : response.getErrcode(),
                response == null ? "wechat official access token response is empty" : response.getErrmsg());
        if (response == null || !StringUtils.hasText(response.getAccessToken())) {
            throw new BizException(502, "wechat official access token is empty");
        }

        long ttl = Math.max(60, (response.getExpiresIn() == null ? 7200 : response.getExpiresIn()) - 120L);
        stringRedisTemplate.opsForValue().set(redisKey, response.getAccessToken(), ttl, TimeUnit.SECONDS);
        return response.getAccessToken();
    }

    private void validateWechatResponse(Integer errCode, String errMsg) {
        if (errCode != null && errCode != 0) {
            throw new BizException(502, "wechat api error: " + errMsg + " (" + errCode + ")");
        }
    }
}
