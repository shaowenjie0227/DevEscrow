package com.programmer.escrow.wechat.model;

import lombok.Data;

@Data
public class WechatUserInfoResponse {

    private Integer subscribe;
    private String openid;
    private String nickname;
    private String headimgurl;
    private Integer errcode;
    private String errmsg;
}
