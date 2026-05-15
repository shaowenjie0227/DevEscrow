package com.programmer.escrow.kb.service;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.kb.dto.KnowledgeBaseSaveDTO;
import com.programmer.escrow.kb.dto.KnowledgeBaseStatusDTO;
import com.programmer.escrow.kb.entity.KnowledgeBaseEntity;
import com.programmer.escrow.kb.mapper.KnowledgeBaseMapper;
import com.programmer.escrow.kb.vo.KnowledgeBaseVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    public KnowledgeBaseServiceImpl(KnowledgeBaseMapper knowledgeBaseMapper) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
    }

    @Override
    public List<KnowledgeBaseVO> listActive() {
        return knowledgeBaseMapper.selectActiveList().stream().map(this::toVO).toList();
    }

    @Override
    public List<KnowledgeBaseVO> listAdmin() {
        return knowledgeBaseMapper.selectAdminList().stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeBaseVO create(KnowledgeBaseSaveDTO dto) {
        KnowledgeBaseEntity entity = new KnowledgeBaseEntity();
        entity.setTitle(dto.getTitle().trim());
        entity.setIntro(dto.getIntro().trim());
        entity.setTechName(dto.getTechName().trim());
        entity.setCoverUrl(dto.getCoverUrl());
        entity.setLinkUrl(dto.getLinkUrl());
        entity.setSortOrder(dto.getSortOrder());
        entity.setStatus(1);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        knowledgeBaseMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeBaseVO update(Long id, KnowledgeBaseSaveDTO dto) {
        KnowledgeBaseEntity entity = getOrThrow(id);
        entity.setTitle(dto.getTitle().trim());
        entity.setIntro(dto.getIntro().trim());
        entity.setTechName(dto.getTechName().trim());
        entity.setCoverUrl(dto.getCoverUrl());
        entity.setLinkUrl(dto.getLinkUrl());
        entity.setSortOrder(dto.getSortOrder());
        entity.setUpdatedAt(LocalDateTime.now());
        knowledgeBaseMapper.update(entity);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO updateStatus(Long id, KnowledgeBaseStatusDTO dto) {
        getOrThrow(id);
        if (!Objects.equals(dto.getStatus(), 1) && !Objects.equals(dto.getStatus(), 2)) {
            throw new BizException(400, "状态不合法");
        }
        knowledgeBaseMapper.updateStatus(id, dto.getStatus());
        return AdminOperationVO.builder()
                .targetId(id)
                .status(dto.getStatus())
                .message(Objects.equals(dto.getStatus(), 1) ? "知识库已启用" : "知识库已停用")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO delete(Long id) {
        getOrThrow(id);
        knowledgeBaseMapper.deleteById(id);
        return AdminOperationVO.builder()
                .targetId(id)
                .status(0)
                .message("知识条目已删除")
                .build();
    }

    @Override
    public KnowledgeBaseEntity getActive(Long id) {
        KnowledgeBaseEntity entity = getOrThrow(id);
        if (!Objects.equals(entity.getStatus(), 1)) {
            throw new BizException(400, "知识库已停用");
        }
        return entity;
    }

    private KnowledgeBaseEntity getOrThrow(Long id) {
        KnowledgeBaseEntity entity = knowledgeBaseMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "知识库不存在");
        }
        return entity;
    }

    private KnowledgeBaseVO toVO(KnowledgeBaseEntity entity) {
        return KnowledgeBaseVO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .intro(entity.getIntro())
                .techName(entity.getTechName())
                .coverUrl(entity.getCoverUrl())
                .linkUrl(entity.getLinkUrl())
                .sortOrder(entity.getSortOrder())
                .status(entity.getStatus())
                .build();
    }
}
