package com.programmer.escrow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        SecurityResponseWriter.writeJson(
                response,
                HttpStatus.UNAUTHORIZED.value(),
                401,
                authException.getMessage() == null ? "authentication failed" : authException.getMessage(),
                objectMapper
        );
    }

    public void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        SecurityResponseWriter.writeJson(response, HttpStatus.UNAUTHORIZED.value(), 401, message, objectMapper);
    }
}
