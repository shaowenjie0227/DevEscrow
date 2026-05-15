package com.programmer.escrow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.programmer.escrow.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class SecurityResponseWriter {

    private SecurityResponseWriter() {
    }

    public static void writeJson(HttpServletResponse response,
                                 int status,
                                 int code,
                                 String message,
                                 ObjectMapper objectMapper) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(code, message)));
        response.getWriter().flush();
    }
}
