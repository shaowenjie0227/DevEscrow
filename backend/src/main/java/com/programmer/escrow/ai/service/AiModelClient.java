package com.programmer.escrow.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.programmer.escrow.ai.model.AiRuntimeConfig;
import com.programmer.escrow.config.properties.AiProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class AiModelClient {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    public AiModelClient(AiProperties aiProperties, ObjectMapper objectMapper) {
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
    }

    public String chat(String systemPrompt, String userPrompt) {
        return chat(systemPrompt, userPrompt, aiProperties.getModel(), aiProperties.getTemperature(), null);
    }

    public String chat(String systemPrompt, String userPrompt, String model, Double temperature) {
        return chat(systemPrompt, userPrompt, model, temperature, null);
    }

    public String chat(String systemPrompt,
                       String userPrompt,
                       String model,
                       Double temperature,
                       AiRuntimeConfig runtimeConfig) {
        if ((runtimeConfig == null && !aiProperties.isEnabled()) || (runtimeConfig != null && !runtimeConfig.isEnabled())) {
            throw new IllegalStateException("AI service is disabled");
        }

        String apiKey = runtimeConfig == null ? aiProperties.getApiKey() : runtimeConfig.getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("AI apiKey is missing");
        }

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1000L, runtimeConfig == null ? aiProperties.getConnectTimeoutMs() : runtimeConfig.getConnectTimeoutMs())))
                .build();

        Map<String, Object> requestBody = buildRequestBody(systemPrompt, userPrompt, model, temperature, runtimeConfig, false);

        String responseBody;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(buildRequestUrl(runtimeConfig)))
                    .timeout(Duration.ofMillis(Math.max(3000L, runtimeConfig == null ? aiProperties.getReadTimeoutMs() : runtimeConfig.getReadTimeoutMs())))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            responseBody = response.body();
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("AI upstream responded with status " + response.statusCode() + ": " + responseBody);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("AI request failed: " + ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI request was interrupted", ex);
        }

        try {
            return extractResponseContent(responseBody);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to parse AI response: " + ex.getMessage(), ex);
        }
    }

    public String chatStream(String systemPrompt,
                             String userPrompt,
                             String model,
                             Double temperature,
                             AiRuntimeConfig runtimeConfig,
                             Consumer<String> chunkConsumer) {
        if ((runtimeConfig == null && !aiProperties.isEnabled()) || (runtimeConfig != null && !runtimeConfig.isEnabled())) {
            throw new IllegalStateException("AI service is disabled");
        }

        String apiKey = runtimeConfig == null ? aiProperties.getApiKey() : runtimeConfig.getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("AI apiKey is missing");
        }

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1000L, runtimeConfig == null ? aiProperties.getConnectTimeoutMs() : runtimeConfig.getConnectTimeoutMs())))
                .build();

        Map<String, Object> requestBody = buildRequestBody(systemPrompt, userPrompt, model, temperature, runtimeConfig, true);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(buildRequestUrl(runtimeConfig)))
                    .timeout(Duration.ofMillis(Math.max(3000L, runtimeConfig == null ? aiProperties.getReadTimeoutMs() : runtimeConfig.getReadTimeoutMs())))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String responseBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new IllegalStateException("AI upstream responded with status " + response.statusCode() + ": " + responseBody);
            }

            String contentType = response.headers().firstValue(HttpHeaders.CONTENT_TYPE).orElse("");
            if (!contentType.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
                String responseBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                return extractResponseContent(responseBody);
            }

            return consumeStreamResponse(response.body(), chunkConsumer);
        } catch (IOException ex) {
            throw new IllegalStateException("AI stream request failed: " + ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI stream request was interrupted", ex);
        }
    }

    private String buildRequestUrl(AiRuntimeConfig runtimeConfig) {
        String baseUrl = runtimeConfig == null ? aiProperties.getBaseUrl() : runtimeConfig.getBaseUrl();
        String chatPath = runtimeConfig == null ? aiProperties.getChatPath() : runtimeConfig.getChatPath();
        baseUrl = baseUrl == null ? "" : baseUrl.trim();
        chatPath = chatPath == null ? "" : chatPath.trim();

        if (baseUrl.endsWith("/") && chatPath.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + chatPath;
        }
        if (!baseUrl.endsWith("/") && !chatPath.startsWith("/")) {
            return baseUrl + "/" + chatPath;
        }
        return baseUrl + chatPath;
    }

    private Map<String, Object> buildRequestBody(String systemPrompt,
                                                 String userPrompt,
                                                 String model,
                                                 Double temperature,
                                                 AiRuntimeConfig runtimeConfig,
                                                 boolean stream) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", StringUtils.hasText(model) ? model : (runtimeConfig == null ? aiProperties.getModel() : runtimeConfig.getModel()));
        requestBody.put("temperature", temperature == null ? (runtimeConfig == null ? aiProperties.getTemperature() : runtimeConfig.getTemperature()) : temperature);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        if (stream) {
            requestBody.put("stream", true);
        }
        return requestBody;
    }

    private String consumeStreamResponse(InputStream inputStream, Consumer<String> chunkConsumer) throws IOException {
        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!StringUtils.hasText(line) || line.startsWith(":")) {
                    continue;
                }
                if (!line.startsWith("data:")) {
                    continue;
                }

                String data = line.substring(5).trim();
                if ("[DONE]".equals(data)) {
                    break;
                }

                JsonNode root = objectMapper.readTree(data);
                String content = root.path("choices").path(0).path("delta").path("content").asText(null);
                if (!StringUtils.hasText(content)) {
                    continue;
                }

                builder.append(content);
                if (chunkConsumer != null) {
                    chunkConsumer.accept(content);
                }
            }
        }

        if (!StringUtils.hasText(builder.toString())) {
            throw new IllegalStateException("AI response content is empty");
        }
        return builder.toString();
    }

    private String extractResponseContent(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        String content = root.path("choices").path(0).path("message").path("content").asText(null);
        if (!StringUtils.hasText(content)) {
            throw new IllegalStateException("AI response content is empty");
        }
        return content;
    }
}
