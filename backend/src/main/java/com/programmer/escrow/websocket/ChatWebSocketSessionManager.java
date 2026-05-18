package com.programmer.escrow.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketSessionManager {

    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public ChatWebSocketSessionManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void register(Long userId, WebSocketSession session) {
        if (userId == null) {
            return;
        }
        userSessions.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void remove(Long userId, WebSocketSession session) {
        if (userId == null) {
            return;
        }
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            userSessions.remove(userId);
        }
    }

    public void notifyChatMessage(Integer bizType,
                                  Long demandId,
                                  Long orderId,
                                  Long senderId,
                                  Long receiverId) {
        ChatSocketMessage payload = ChatSocketMessage.builder()
                .type("CHAT_MESSAGE")
                .bizType(bizType)
                .demandId(demandId)
                .orderId(orderId)
                .senderId(senderId)
                .receiverId(receiverId)
                .build();
        send(senderId, payload);
        if (receiverId != null && !receiverId.equals(senderId)) {
            send(receiverId, payload);
        }
    }

    public void notifySiteMessage(Long receiverId, Long messageId) {
        if (receiverId == null || messageId == null) {
            return;
        }
        SiteMessageSocketMessage payload = SiteMessageSocketMessage.builder()
                .type("SITE_MESSAGE")
                .messageId(messageId)
                .build();
        send(receiverId, payload);
    }

    private void send(Long userId, Object payload) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        try {
            String text = objectMapper.writeValueAsString(payload);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(text));
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("failed to push websocket message", ex);
        }
    }
}
