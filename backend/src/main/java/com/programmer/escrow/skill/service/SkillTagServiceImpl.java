package com.programmer.escrow.skill.service;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.skill.dto.SkillTagSaveDTO;
import com.programmer.escrow.skill.dto.SkillTagStatusDTO;
import com.programmer.escrow.skill.entity.SkillTagEntity;
import com.programmer.escrow.skill.mapper.SkillTagMapper;
import com.programmer.escrow.skill.vo.SkillTagVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class SkillTagServiceImpl implements SkillTagService {

    private final SkillTagMapper skillTagMapper;

    public SkillTagServiceImpl(SkillTagMapper skillTagMapper) {
        this.skillTagMapper = skillTagMapper;
    }

    @Override
    public List<SkillTagVO> listActive() {
        return skillTagMapper.selectActiveList().stream().map(this::toVO).toList();
    }

    @Override
    public List<SkillTagVO> listAdmin() {
        return skillTagMapper.selectAdminList().stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SkillTagVO create(SkillTagSaveDTO dto) {
        String tagName = normalize(dto.getTagName());
        ensureUnique(tagName, null);
        SkillTagEntity entity = new SkillTagEntity();
        entity.setTagName(tagName);
        entity.setTagType(normalize(dto.getTagType()));
        entity.setSortOrder(dto.getSortOrder());
        entity.setStatus(1);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        skillTagMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SkillTagVO update(Long tagId, SkillTagSaveDTO dto) {
        SkillTagEntity entity = getTagOrThrow(tagId);
        String tagName = normalize(dto.getTagName());
        ensureUnique(tagName, tagId);
        entity.setTagName(tagName);
        entity.setTagType(normalize(dto.getTagType()));
        entity.setSortOrder(dto.getSortOrder());
        entity.setUpdatedAt(LocalDateTime.now());
        skillTagMapper.update(entity);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO updateStatus(Long tagId, SkillTagStatusDTO dto) {
        getTagOrThrow(tagId);
        if (!Objects.equals(dto.getStatus(), 1) && !Objects.equals(dto.getStatus(), 2)) {
            throw new BizException(400, "状态不合法");
        }
        skillTagMapper.updateStatus(tagId, dto.getStatus());
        return AdminOperationVO.builder()
                .targetId(tagId)
                .status(dto.getStatus())
                .message(Objects.equals(dto.getStatus(), 1) ? "技能栈已启用" : "技能栈已停用")
                .build();
    }

    @Override
    public SkillTagEntity getActiveTag(Long tagId) {
        SkillTagEntity entity = getTagOrThrow(tagId);
        if (!Objects.equals(entity.getStatus(), 1)) {
            throw new BizException(400, "所选技能栈已停用，请重新选择");
        }
        return entity;
    }

    private SkillTagEntity getTagOrThrow(Long tagId) {
        SkillTagEntity entity = skillTagMapper.selectById(tagId);
        if (entity == null) {
            throw new BizException(404, "技能栈不存在");
        }
        return entity;
    }

    private void ensureUnique(String tagName, Long currentId) {
        SkillTagEntity existing = skillTagMapper.selectByName(tagName);
        if (existing != null && !Objects.equals(existing.getId(), currentId)) {
            throw new BizException(400, "技能栈名称已存在");
        }
    }

    private String normalize(String text) {
        return text == null ? null : text.trim();
    }

    private SkillTagVO toVO(SkillTagEntity entity) {
        return SkillTagVO.builder()
                .id(entity.getId())
                .tagName(entity.getTagName())
                .tagType(entity.getTagType())
                .sortOrder(entity.getSortOrder())
                .status(entity.getStatus())
                .build();
    }
}
