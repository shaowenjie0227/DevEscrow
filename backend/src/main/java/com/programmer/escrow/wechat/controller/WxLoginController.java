package com.programmer.escrow.wechat.controller;

import com.programmer.escrow.auth.model.WxCallbackResult;
import com.programmer.escrow.auth.service.QrLoginService;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.common.util.ClientIpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

@Controller
public class WxLoginController {

    private final QrLoginService qrLoginService;

    public WxLoginController(QrLoginService qrLoginService) {
        this.qrLoginService = qrLoginService;
    }

    @GetMapping("/wx/login")
    public ResponseEntity<String> login(@RequestParam("token") String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(qrLoginService.buildWechatAuthorizeUrl(token)));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } catch (BizException ex) {
            return htmlPage("QR expired", ex.getMessage());
        }
    }

    @GetMapping("/wx/callback")
    public ResponseEntity<String> callback(@RequestParam("code") String code,
                                           @RequestParam("state") String state,
                                           HttpServletRequest request) {
        try {
            WxCallbackResult result = qrLoginService.handleWechatCallback(code, state, ClientIpUtils.resolve(request));
            if (result.isFollowRequired()) {
                HttpHeaders headers = new HttpHeaders();
                headers.setLocation(URI.create(result.getFollowUrl()));
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
            }
            return htmlPage(result.getTitle(), result.getMessage());
        } catch (BizException ex) {
            return htmlPage("Login failed", ex.getMessage());
        }
    }

    @GetMapping("/wx/login-entry")
    public ResponseEntity<String> loginEntry(@RequestParam(value = "code", required = false) String code) {
        String html = "<!doctype html>"
                + "<html lang=\"en\">"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<title>Official Account PC Login</title>"
                + "<style>"
                + "body{font-family:Segoe UI,Arial,sans-serif;background:#f8fafc;margin:0;display:flex;align-items:center;justify-content:center;min-height:100vh;color:#0f172a}"
                + ".card{width:min(92vw,460px);background:#fff;border-radius:20px;padding:32px;box-shadow:0 20px 50px rgba(15,23,42,.12)}"
                + "h1{margin:0 0 12px;font-size:24px}"
                + "p{margin:0 0 20px;line-height:1.7;color:#475569}"
                + "input{width:100%;padding:14px 16px;border:1px solid #cbd5e1;border-radius:14px;font-size:18px;letter-spacing:0.2em;box-sizing:border-box}"
                + "button{margin-top:16px;width:100%;padding:14px 16px;border:none;border-radius:14px;background:#16a34a;color:#fff;font-size:16px;cursor:pointer}"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class=\"card\">"
                + "<h1>Enter the 6-digit code from your PC</h1>"
                + "<p>After following the official account, enter the temporary code shown on the PC site to bind this browser session to the correct computer.</p>"
                + "<form method=\"get\" action=\"/wx/login/authorize\">"
                + "<input name=\"loginCode\" maxlength=\"6\" placeholder=\"for example 125680\" value=\"" + (code == null ? "" : code) + "\" />"
                + "<button type=\"submit\">Continue WeChat Authorization</button>"
                + "</form>"
                + "</div>"
                + "</body>"
                + "</html>";
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    @GetMapping("/wx/login/authorize")
    public ResponseEntity<String> authorizeByCode(@RequestParam("loginCode") String loginCode) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(qrLoginService.buildWechatAuthorizeUrlByCode(loginCode)));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } catch (BizException ex) {
            return htmlPage("Login code expired", ex.getMessage());
        }
    }

    private ResponseEntity<String> htmlPage(String title, String message) {
        String html = "<!doctype html>"
                + "<html lang=\"en\">"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<title>" + title + "</title>"
                + "<style>"
                + "body{font-family:Segoe UI,Arial,sans-serif;background:#f4f7fb;margin:0;display:flex;align-items:center;justify-content:center;min-height:100vh;color:#0f172a}"
                + ".card{width:min(90vw,420px);background:#fff;border-radius:20px;padding:32px;box-shadow:0 20px 50px rgba(15,23,42,.12)}"
                + "h1{margin:0 0 12px;font-size:24px}"
                + "p{margin:0;line-height:1.7;color:#475569}"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class=\"card\">"
                + "<h1>" + title + "</h1>"
                + "<p>" + message + "</p>"
                + "</div>"
                + "</body>"
                + "</html>";
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}
