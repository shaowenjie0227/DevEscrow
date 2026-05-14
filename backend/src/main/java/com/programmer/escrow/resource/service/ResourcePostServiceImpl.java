package com.programmer.escrow.resource.service;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.resource.dto.ResourcePostSaveDTO;
import com.programmer.escrow.resource.dto.ResourcePostStatusDTO;
import com.programmer.escrow.resource.entity.ResourcePostEntity;
import com.programmer.escrow.resource.mapper.ResourcePostMapper;
import com.programmer.escrow.resource.vo.ResourcePostVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ResourcePostServiceImpl implements ResourcePostService {

    private final ResourcePostMapper resourcePostMapper;

    public ResourcePostServiceImpl(ResourcePostMapper resourcePostMapper) {
        this.resourcePostMapper = resourcePostMapper;
    }

    @Override
    public List<ResourcePostVO> listActive() {
        return resourcePostMapper.selectActiveList().stream().map(this::toVO).toList();
    }

    @Override
    public List<ResourcePostVO> listAdmin() {
        return resourcePostMapper.selectAdminList().stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourcePostVO create(Long creatorId, ResourcePostSaveDTO dto) {
        ResourcePostEntity entity = new ResourcePostEntity();
        entity.setTitle(dto.getTitle().trim());
        entity.setIntro(dto.getIntro().trim());
        entity.setCoverUrl(dto.getCoverUrl());
        entity.setLinkUrl(dto.getLinkUrl());
        entity.setSortOrder(dto.getSortOrder());
        entity.setStatus(1);
        entity.setLikeCount(0);
        entity.setFavoriteCount(0);
        entity.setShareCount(0);
        entity.setCreatorId(creatorId);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        resourcePostMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourcePostVO update(Long id, ResourcePostSaveDTO dto) {
        ResourcePostEntity entity = getOrThrow(id);
        entity.setTitle(dto.getTitle().trim());
        entity.setIntro(dto.getIntro().trim());
        entity.setCoverUrl(dto.getCoverUrl());
        entity.setLinkUrl(dto.getLinkUrl());
        entity.setSortOrder(dto.getSortOrder());
        entity.setUpdatedAt(LocalDateTime.now());
        resourcePostMapper.update(entity);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO updateStatus(Long id, ResourcePostStatusDTO dto) {
        getOrThrow(id);
        if (!Objects.equals(dto.getStatus(), 1) && !Objects.equals(dto.getStatus(), 2)) {
            throw new BizException(400, "状态不合法");
        }
        resourcePostMapper.updateStatus(id, dto.getStatus());
        return AdminOperationVO.builder()
                .targetId(id)
                .status(dto.getStatus())
                .message(Objects.equals(dto.getStatus(), 1) ? "资源已启用" : "资源已停用")
                .build();
    }

    @Override
    public ResourcePostEntity getActive(Long id) {
        ResourcePostEntity entity = getOrThrow(id);
        if (!Objects.equals(entity.getStatus(), 1)) {
            throw new BizException(400, "资源已停用");
        }
        return entity;
    }

    @Override
    public void like(Long id) {
        getActive(id);
        resourcePostMapper.incrementLike(id);
    }

    @Override
    public void favorite(Long id) {
        getActive(id);
        resourcePostMapper.incrementFavorite(id);
    }

    @Override
    public void share(Long id) {
        getActive(id);
        resourcePostMapper.incrementShare(id);
    }

    private ResourcePostEntity getOrThrow(Long id) {
        ResourcePostEntity entity = resourcePostMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "资源不存在");
        }
        return entity;
    }

    private ResourcePostVO toVO(ResourcePostEntity entity) {
        return ResourcePostVO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .intro(entity.getIntro())
                .coverUrl(entity.getCoverUrl())
                .linkUrl(entity.getLinkUrl())
                .sortOrder(entity.getSortOrder())
                .status(entity.getStatus())
                .likeCount(entity.getLikeCount())
                .favoriteCount(entity.getFavoriteCount())
                .shareCount(entity.getShareCount())
                .build();
    }
}
