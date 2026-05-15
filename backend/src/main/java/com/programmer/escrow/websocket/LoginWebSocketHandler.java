package com.programmer.escrow.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class LoginWebSocketHandler extends TextWebSocketHandler {

    private final LoginWebSocketSessionManager sessionManager;

    public LoginWebSocketHandler(LoginWebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String loginToken = (String) session.getAttributes().get(LoginWebSocketHandshakeInterceptor.LOGIN_TOKEN_ATTR);
        sessionManager.register(loginToken, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage("{\"type\":\"PONG\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String loginToken = (String) session.getAttributes().get(LoginWebSocketHandshakeInterceptor.LOGIN_TOKEN_ATTR);
        sessionManager.remove(loginToken, session);
    }
}
