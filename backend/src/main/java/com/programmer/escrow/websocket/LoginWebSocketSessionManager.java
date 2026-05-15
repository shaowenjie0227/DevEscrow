package com.programmer.escrow.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.programmer.escrow.auth.util.AuthRoleResolver;
import com.programmer.escrow.security.model.IssuedJwtToken;
import com.programmer.escrow.user.entity.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginWebSocketSessionManager {

    private final Map<String, Set<WebSocketSession>> tokenSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public LoginWebSocketSessionManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void register(String token, WebSocketSession session) {
        tokenSessions.computeIfAbsent(token, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void remove(String token, WebSocketSession session) {
        Set<WebSocketSession> sessions = tokenSessions.get(token);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            tokenSessions.remove(token);
        }
    }

    public void notifyLoginSuccess(String token, IssuedJwtToken jwtToken, UserEntity user) {
        Set<WebSocketSession> sessions = tokenSessions.get(token);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        LoginSuccessMessage message = LoginSuccessMessage.builder()
                .type("LOGIN_SUCCESS")
                .token(token)
                .jwt(jwtToken.token())
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .userType(user.getUserType())
                .roles(AuthRoleResolver.resolveRoles(user.getUserType(), user.getDeveloperStatus()))
                .redirectPath(AuthRoleResolver.resolveRedirectPath())
                .build();
        try {
            String payload = objectMapper.writeValueAsString(message);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(payload));
                    session.close();
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("failed to push login websocket message", ex);
        } finally {
            tokenSessions.remove(token);
        }
    }
}
