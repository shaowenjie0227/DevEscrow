package com.programmer.escrow.ai.service;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.programmer.escrow.ai.dto.AiRuntimeConfigSaveDTO;
import com.programmer.escrow.ai.entity.AiRuntimeConfigEntity;
import com.programmer.escrow.ai.mapper.AiRuntimeConfigMapper;
import com.programmer.escrow.ai.model.AiRuntimeConfig;
import com.programmer.escrow.ai.vo.AiRuntimeConfigVO;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.config.properties.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
public class AiRuntimeConfigService {

    private static final Logger log = LoggerFactory.getLogger(AiRuntimeConfigService.class);
    public static final String DEFAULT_CONFIG_KEY = "DEFAULT";

    private final AiRuntimeConfigMapper aiRuntimeConfigMapper;
    private final AiProperties aiProperties;

    public AiRuntimeConfigService(AiRuntimeConfigMapper aiRuntimeConfigMapper, AiProperties aiProperties) {
        this.aiRuntimeConfigMapper = aiRuntimeConfigMapper;
        this.aiProperties = aiProperties;
    }

    public AiRuntimeConfig resolveEffectiveConfig() {
        AiRuntimeConfigEntity entity = aiRuntimeConfigMapper.selectByConfigKey(DEFAULT_CONFIG_KEY);
        if (entity == null) {
            return fromProperties();
        }
        String decryptedApiKey = decryptSafely(entity.getApiKeyCiphertext(), entity.getConfigKey(), false);
        return AiRuntimeConfig.builder()
                .enabled(entity.getEnabled() == null ? aiProperties.isEnabled() : entity.getEnabled() == 1)
                .fallbackEnabled(entity.getFallbackEnabled() == null ? aiProperties.isFallbackEnabled() : entity.getFallbackEnabled() == 1)
                .provider(defaultIfBlank(entity.getProvider(), aiProperties.getProvider()))
                .baseUrl(defaultIfBlank(entity.getBaseUrl(), aiProperties.getBaseUrl()))
                .chatPath(defaultIfBlank(entity.getChatPath(), aiProperties.getChatPath()))
                .apiKey(defaultIfBlank(decryptedApiKey, aiProperties.getApiKey()))
                .model(defaultIfBlank(entity.getModel(), aiProperties.getModel()))
                .temperature(entity.getTemperature() == null ? aiProperties.getTemperature() : entity.getTemperature())
                .topK(entity.getTopK() == null ? aiProperties.getTopK() : entity.getTopK())
                .cacheTtlSeconds(entity.getCacheTtlSeconds() == null ? aiProperties.getCacheTtlSeconds() : entity.getCacheTtlSeconds())
                .connectTimeoutMs(entity.getConnectTimeoutMs() == null ? aiProperties.getConnectTimeoutMs() : entity.getConnectTimeoutMs())
                .readTimeoutMs(entity.getReadTimeoutMs() == null ? aiProperties.getReadTimeoutMs() : entity.getReadTimeoutMs())
                .build();
    }

    public AiRuntimeConfigVO getAdminConfig() {
        AiRuntimeConfigEntity entity = aiRuntimeConfigMapper.selectByConfigKey(DEFAULT_CONFIG_KEY);
        if (entity == null) {
            return AiRuntimeConfigVO.builder()
                    .configKey(DEFAULT_CONFIG_KEY)
                    .enabled(aiProperties.isEnabled())
                    .provider(aiProperties.getProvider())
                    .baseUrl(aiProperties.getBaseUrl())
                    .chatPath(aiProperties.getChatPath())
                    .maskedApiKey(maskApiKey(aiProperties.getApiKey()))
                    .hasApiKey(StringUtils.hasText(aiProperties.getApiKey()))
                    .model(aiProperties.getModel())
                    .temperature(aiProperties.getTemperature())
                    .topK(aiProperties.getTopK())
                    .fallbackEnabled(aiProperties.isFallbackEnabled())
                    .cacheTtlSeconds(aiProperties.getCacheTtlSeconds())
                    .connectTimeoutMs(aiProperties.getConnectTimeoutMs())
                    .readTimeoutMs(aiProperties.getReadTimeoutMs())
                    .build();
        }
        String decryptedApiKey = decryptSafely(entity.getApiKeyCiphertext(), entity.getConfigKey(), true);
        String effectiveApiKey = defaultIfBlank(decryptedApiKey, aiProperties.getApiKey());
        return AiRuntimeConfigVO.builder()
                .id(entity.getId())
                .configKey(entity.getConfigKey())
                .enabled(entity.getEnabled() == 1)
                .provider(entity.getProvider())
                .baseUrl(entity.getBaseUrl())
                .chatPath(entity.getChatPath())
                .maskedApiKey(maskApiKey(effectiveApiKey))
                .hasApiKey(StringUtils.hasText(effectiveApiKey))
                .model(entity.getModel())
                .temperature(entity.getTemperature())
                .topK(entity.getTopK())
                .fallbackEnabled(entity.getFallbackEnabled() == 1)
                .cacheTtlSeconds(entity.getCacheTtlSeconds())
                .connectTimeoutMs(entity.getConnectTimeoutMs())
                .readTimeoutMs(entity.getReadTimeoutMs())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public AiRuntimeConfigVO save(AiRuntimeConfigSaveDTO dto) {
        AiRuntimeConfigEntity entity = aiRuntimeConfigMapper.selectByConfigKey(DEFAULT_CONFIG_KEY);
        if (entity == null) {
            entity = new AiRuntimeConfigEntity();
            entity.setConfigKey(DEFAULT_CONFIG_KEY);
            entity.setCreatedAt(LocalDateTime.now());
        }

        entity.setEnabled(Boolean.TRUE.equals(dto.getEnabled()) ? 1 : 0);
        entity.setProvider(dto.getProvider().trim());
        entity.setBaseUrl(dto.getBaseUrl().trim());
        entity.setChatPath(dto.getChatPath().trim());
        entity.setModel(dto.getModel().trim());
        entity.setTemperature(dto.getTemperature());
        entity.setTopK(dto.getTopK());
        entity.setFallbackEnabled(Boolean.TRUE.equals(dto.getFallbackEnabled()) ? 1 : 0);
        entity.setCacheTtlSeconds(dto.getCacheTtlSeconds());
        entity.setConnectTimeoutMs(dto.getConnectTimeoutMs());
        entity.setReadTimeoutMs(dto.getReadTimeoutMs());
        entity.setUpdatedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(dto.getClearApiKey())) {
            entity.setApiKeyCiphertext(null);
        } else if (StringUtils.hasText(dto.getApiKey())) {
            entity.setApiKeyCiphertext(encrypt(dto.getApiKey().trim()));
        }

        if (entity.getId() == null) {
            aiRuntimeConfigMapper.insert(entity);
        } else {
            aiRuntimeConfigMapper.update(entity);
        }
        return getAdminConfig();
    }

    private AiRuntimeConfig fromProperties() {
        return AiRuntimeConfig.builder()
                .enabled(aiProperties.isEnabled())
                .fallbackEnabled(aiProperties.isFallbackEnabled())
                .provider(aiProperties.getProvider())
                .baseUrl(aiProperties.getBaseUrl())
                .chatPath(aiProperties.getChatPath())
                .apiKey(aiProperties.getApiKey())
                .model(aiProperties.getModel())
                .temperature(aiProperties.getTemperature())
                .topK(aiProperties.getTopK())
                .cacheTtlSeconds(aiProperties.getCacheTtlSeconds())
                .connectTimeoutMs(aiProperties.getConnectTimeoutMs())
                .readTimeoutMs(aiProperties.getReadTimeoutMs())
                .build();
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String encrypt(String plaintext) {
        if (!StringUtils.hasText(plaintext)) {
            return null;
        }
        return crypto().encryptBase64(plaintext, StandardCharsets.UTF_8);
    }

    private String decrypt(String ciphertext) {
        if (!StringUtils.hasText(ciphertext)) {
            return null;
        }
        try {
            return crypto().decryptStr(ciphertext, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new BizException(500, "AI 配置密钥解密失败，请检查加密配置");
        }
    }

    private String decryptSafely(String ciphertext, String configKey, boolean exposeError) {
        if (!StringUtils.hasText(ciphertext)) {
            return null;
        }
        try {
            return decrypt(ciphertext);
        } catch (BizException ex) {
            log.warn("AI runtime config apiKey decrypt failed for configKey={}, fallback to properties/default flow", configKey);
            if (exposeError && !StringUtils.hasText(aiProperties.getApiKey())) {
                return null;
            }
            return null;
        }
    }

    private SymmetricCrypto crypto() {
        String cryptoKey = StringUtils.hasText(aiProperties.getConfigCryptoKey())
                ? aiProperties.getConfigCryptoKey()
                : "change-this-ai-config-crypto-key-2026";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] key = messageDigest.digest(cryptoKey.getBytes(StandardCharsets.UTF_8));
            return SecureUtil.aes(key);
        } catch (Exception ex) {
            throw new BizException(500, "AI 配置加密初始化失败");
        }
    }

    private String maskApiKey(String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            return "";
        }
        if (apiKey.length() <= 8) {
            return "********";
        }
        return apiKey.substring(0, 4) + "********" + apiKey.substring(apiKey.length() - 4);
    }
}
