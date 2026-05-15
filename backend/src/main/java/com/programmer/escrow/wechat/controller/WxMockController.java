package com.programmer.escrow.wechat.controller;

import com.programmer.escrow.config.properties.WechatProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class WxMockController {

    private final WechatProperties wechatProperties;

    public WxMockController(WechatProperties wechatProperties) {
        this.wechatProperties = wechatProperties;
    }

    @GetMapping("/wx/mock/authorize")
    public ResponseEntity<Void> authorize(@RequestParam("state") String state,
                                          @RequestParam(value = "subscribed", required = false) Integer subscribed) {
        int subscribeFlag = subscribed == null
                ? (wechatProperties.getMock().isSubscribed() ? 1 : 0)
                : subscribed;
        String code = "MOCK|" + wechatProperties.getMock().getOpenid() + "|" + subscribeFlag;
        String redirect = "/wx/callback?code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirect)).build();
    }

    @GetMapping("/wx/mock/follow")
    public ResponseEntity<String> follow(@RequestParam(value = "state", required = false) String state) {
        String continueUrl = "/wx/mock/authorize?subscribed=1";
        if (state != null) {
            continueUrl += "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
        }

        String html = "<!doctype html>"
                + "<html lang=\"en\">"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<title>Mock Follow Page</title>"
                + "<style>"
                + "body{font-family:Segoe UI,Arial,sans-serif;background:#e8fff2;margin:0;display:flex;align-items:center;justify-content:center;min-height:100vh;color:#064e3b}"
                + ".card{width:min(90vw,460px);background:#fff;border-radius:20px;padding:32px;box-shadow:0 20px 50px rgba(6,78,59,.15)}"
                + "h1{margin:0 0 12px;font-size:24px}"
                + "p{margin:0 0 20px;line-height:1.7;color:#166534}"
                + "a{display:inline-block;padding:12px 18px;border-radius:999px;background:#16a34a;color:#fff;text-decoration:none}"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class=\"card\">"
                + "<h1>Mock Follow Page</h1>"
                + "<p>This is the local mock page for official-account subscription. Click the button below to simulate a successful follow and finish the OAuth callback.</p>"
                + "<a href=\"" + continueUrl + "\">Simulate follow and continue</a>"
                + "</div>"
                + "</body>"
                + "</html>";
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}
