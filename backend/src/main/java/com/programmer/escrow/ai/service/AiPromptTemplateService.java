package com.programmer.escrow.ai.service;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.ai.dto.AiPromptTemplateSaveDTO;
import com.programmer.escrow.ai.dto.AiPromptTemplateStatusDTO;
import com.programmer.escrow.ai.entity.AiPromptTemplateEntity;
import com.programmer.escrow.ai.mapper.AiPromptTemplateMapper;
import com.programmer.escrow.ai.vo.AiPromptTemplateVO;
import com.programmer.escrow.common.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class AiPromptTemplateService {

    private final AiPromptTemplateMapper aiPromptTemplateMapper;

    public AiPromptTemplateService(AiPromptTemplateMapper aiPromptTemplateMapper) {
        this.aiPromptTemplateMapper = aiPromptTemplateMapper;
    }

    public List<AiPromptTemplateVO> listAdmin() {
        return aiPromptTemplateMapper.selectAdminList().stream().map(this::toVO).toList();
    }

    public AiPromptTemplateEntity getActiveBySceneCode(String sceneCode) {
        return aiPromptTemplateMapper.selectActiveBySceneCode(normalizeSceneCode(sceneCode));
    }

    @Transactional(rollbackFor = Exception.class)
    public AiPromptTemplateVO create(AiPromptTemplateSaveDTO dto) {
        String sceneCode = normalizeSceneCode(dto.getSceneCode());
        ensureUnique(sceneCode, null);

        AiPromptTemplateEntity entity = new AiPromptTemplateEntity();
        entity.setSceneCode(sceneCode);
        entity.setSceneName(dto.getSceneName().trim());
        entity.setSystemPrompt(normalizeText(dto.getSystemPrompt()));
        entity.setUserPromptTemplate(normalizeText(dto.getUserPromptTemplate()));
        entity.setModel(normalizeText(dto.getModel()));
        entity.setTemperature(dto.getTemperature());
        entity.setTopK(dto.getTopK());
        entity.setStatus(1);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        aiPromptTemplateMapper.insert(entity);
        return toVO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public AiPromptTemplateVO update(Long id, AiPromptTemplateSaveDTO dto) {
        AiPromptTemplateEntity entity = getOrThrow(id);
        String sceneCode = normalizeSceneCode(dto.getSceneCode());
        ensureUnique(sceneCode, id);

        entity.setSceneCode(sceneCode);
        entity.setSceneName(dto.getSceneName().trim());
        entity.setSystemPrompt(normalizeText(dto.getSystemPrompt()));
        entity.setUserPromptTemplate(normalizeText(dto.getUserPromptTemplate()));
        entity.setModel(normalizeText(dto.getModel()));
        entity.setTemperature(dto.getTemperature());
        entity.setTopK(dto.getTopK());
        entity.setUpdatedAt(LocalDateTime.now());
        aiPromptTemplateMapper.update(entity);
        return toVO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO updateStatus(Long id, AiPromptTemplateStatusDTO dto) {
        getOrThrow(id);
        if (!Objects.equals(dto.getStatus(), 1) && !Objects.equals(dto.getStatus(), 2)) {
            throw new BizException(400, "Prompt 模板状态不合法");
        }
        aiPromptTemplateMapper.updateStatus(id, dto.getStatus());
        return AdminOperationVO.builder()
                .targetId(id)
                .status(dto.getStatus())
                .message(Objects.equals(dto.getStatus(), 1) ? "Prompt 模板已启用" : "Prompt 模板已停用")
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO delete(Long id) {
        getOrThrow(id);
        aiPromptTemplateMapper.deleteById(id);
        return AdminOperationVO.builder()
                .targetId(id)
                .status(0)
                .message("Prompt 模板已删除")
                .build();
    }

    private void ensureUnique(String sceneCode, Long currentId) {
        AiPromptTemplateEntity existing = aiPromptTemplateMapper.selectBySceneCode(sceneCode);
        if (existing != null && !Objects.equals(existing.getId(), currentId)) {
            throw new BizException(400, "该场景编码已经存在 Prompt 配置");
        }
    }

    private AiPromptTemplateEntity getOrThrow(Long id) {
        AiPromptTemplateEntity entity = aiPromptTemplateMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "Prompt 模板不存在");
        }
        return entity;
    }

    private String normalizeSceneCode(String sceneCode) {
        return sceneCode == null ? null : sceneCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String text) {
        return text == null ? null : text.trim();
    }

    private AiPromptTemplateVO toVO(AiPromptTemplateEntity entity) {
        return AiPromptTemplateVO.builder()
                .id(entity.getId())
                .sceneCode(entity.getSceneCode())
                .sceneName(entity.getSceneName())
                .systemPrompt(entity.getSystemPrompt())
                .userPromptTemplate(entity.getUserPromptTemplate())
                .model(entity.getModel())
                .temperature(entity.getTemperature())
                .topK(entity.getTopK())
                .status(entity.getStatus())
                .build();
    }
}
