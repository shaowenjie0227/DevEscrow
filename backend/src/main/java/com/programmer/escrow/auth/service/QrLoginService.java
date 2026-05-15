package com.programmer.escrow.auth.service;

import com.programmer.escrow.auth.model.WxCallbackResult;
import com.programmer.escrow.auth.vo.QrCodeCreateVO;

public interface QrLoginService {

    QrCodeCreateVO createLoginQr();

    String buildWechatAuthorizeUrl(String token);

    String buildWechatAuthorizeUrlByCode(String loginCode);

    WxCallbackResult handleWechatCallback(String code, String token, String loginIp);
}
