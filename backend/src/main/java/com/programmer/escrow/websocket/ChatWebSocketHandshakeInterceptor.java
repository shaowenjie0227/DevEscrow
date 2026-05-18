package com.programmer.escrow.websocket;

import com.programmer.escrow.security.JwtSessionService;
import com.programmer.escrow.security.JwtUtil;
import com.programmer.escrow.security.model.JwtUserClaims;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import com.programmer.escrow.user.service.UserBanLifecycleService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class ChatWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    public static final String CHAT_USER_ID_ATTR = "chatUserId";

    private final JwtUtil jwtUtil;
    private final JwtSessionService jwtSessionService;
    private final UserMapper userMapper;
    private final UserBanLifecycleService userBanLifecycleService;

    public ChatWebSocketHandshakeInterceptor(JwtUtil jwtUtil,
                                             JwtSessionService jwtSessionService,
                                             UserMapper userMapper,
                                             UserBanLifecycleService userBanLifecycleService) {
        this.jwtUtil = jwtUtil;
        this.jwtSessionService = jwtSessionService;
        this.userMapper = userMapper;
        this.userBanLifecycleService = userBanLifecycleService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        if (query == null || query.isBlank()) {
            return false;
        }
        String token = null;
        for (String item : query.split("&")) {
            if (item.startsWith("token=")) {
                token = URLDecoder.decode(item.substring("token=".length()), StandardCharsets.UTF_8);
                break;
            }
        }
        if (token == null || token.isBlank() || jwtSessionService.isBlacklisted(token)) {
            return false;
        }
        try {
            JwtUserClaims claims = jwtUtil.parseToken(token);
            UserEntity user = userBanLifecycleService.normalizeBanStatus(userMapper.selectById(claims.userId()));
            if (user == null || user.getStatus() == null || user.getStatus() != 1) {
                return false;
            }
            attributes.put(CHAT_USER_ID_ATTR, claims.userId());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }
}
