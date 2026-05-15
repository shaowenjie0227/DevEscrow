package com.programmer.escrow.websocket;

import com.programmer.escrow.config.properties.CorsProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final LoginWebSocketHandler loginWebSocketHandler;
    private final LoginWebSocketHandshakeInterceptor handshakeInterceptor;
    private final CorsProperties corsProperties;

    public WebSocketConfig(LoginWebSocketHandler loginWebSocketHandler,
                           LoginWebSocketHandshakeInterceptor handshakeInterceptor,
                           CorsProperties corsProperties) {
        this.loginWebSocketHandler = loginWebSocketHandler;
        this.handshakeInterceptor = handshakeInterceptor;
        this.corsProperties = corsProperties;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(loginWebSocketHandler, "/ws/login/*")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new));
    }
}
