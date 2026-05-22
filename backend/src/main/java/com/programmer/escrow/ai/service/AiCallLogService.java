package com.programmer.escrow.ai.service;

import com.programmer.escrow.ai.entity.AiCallLogEntity;
import com.programmer.escrow.ai.mapper.AiCallLogMapper;
import com.programmer.escrow.ai.model.AiCallLogCreateCommand;
import com.programmer.escrow.ai.vo.AiCallLogVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AiCallLogService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AiCallLogMapper aiCallLogMapper;

    public AiCallLogService(AiCallLogMapper aiCallLogMapper) {
        this.aiCallLogMapper = aiCallLogMapper;
    }

    public void create(AiCallLogCreateCommand command) {
        AiCallLogEntity entity = new AiCallLogEntity();
        entity.setSceneCode(command.getSceneCode());
        entity.setPromptVersion(command.getPromptVersion());
        entity.setProvider(command.getProvider());
        entity.setModel(command.getModel());
        entity.setCreatorUserId(command.getCreatorUserId());
        entity.setRequestDigest(command.getRequestDigest());
        entity.setRequestPreview(trimTo(command.getRequestPreview(), 500));
        entity.setContextCount(command.getContextCount());
        entity.setCacheHit(Boolean.TRUE.equals(command.getCacheHit()) ? 1 : 0);
        entity.setFallbackUsed(Boolean.TRUE.equals(command.getFallbackUsed()) ? 1 : 0);
        entity.setStatus(command.getStatus());
        entity.setLatencyMs(command.getLatencyMs());
        entity.setErrorMessage(trimTo(command.getErrorMessage(), 500));
        entity.setResultPreview(trimTo(command.getResultPreview(), 1500));
        entity.setCreatedAt(LocalDateTime.now());
        aiCallLogMapper.insert(entity);
    }

    public List<AiCallLogVO> listRecent(String sceneCode, Integer status, Integer limit) {
        int safeLimit = limit == null ? 50 : Math.max(1, Math.min(limit, 200));
        return aiCallLogMapper.selectRecent(sceneCode, status, safeLimit).stream().map(this::toVO).toList();
    }

    private String trimTo(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength) + "...";
    }

    private AiCallLogVO toVO(AiCallLogEntity entity) {
        return AiCallLogVO.builder()
                .id(entity.getId())
                .sceneCode(entity.getSceneCode())
                .promptVersion(entity.getPromptVersion())
                .provider(entity.getProvider())
                .model(entity.getModel())
                .creatorUserId(entity.getCreatorUserId())
                .requestPreview(entity.getRequestPreview())
                .contextCount(entity.getContextCount())
                .cacheHit(entity.getCacheHit())
                .fallbackUsed(entity.getFallbackUsed())
                .status(entity.getStatus())
                .latencyMs(entity.getLatencyMs())
                .errorMessage(entity.getErrorMessage())
                .resultPreview(entity.getResultPreview())
                .createdAt(entity.getCreatedAt() == null ? null : entity.getCreatedAt().format(TIME_FORMATTER))
                .build();
    }
}
