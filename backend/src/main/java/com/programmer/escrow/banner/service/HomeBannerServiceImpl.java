package com.programmer.escrow.banner.service;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.banner.dto.HomeBannerSaveDTO;
import com.programmer.escrow.banner.dto.HomeBannerStatusDTO;
import com.programmer.escrow.banner.entity.HomeBannerEntity;
import com.programmer.escrow.banner.mapper.HomeBannerMapper;
import com.programmer.escrow.banner.vo.HomeBannerVO;
import com.programmer.escrow.common.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class HomeBannerServiceImpl implements HomeBannerService {

    private final HomeBannerMapper homeBannerMapper;

    public HomeBannerServiceImpl(HomeBannerMapper homeBannerMapper) {
        this.homeBannerMapper = homeBannerMapper;
    }

    @Override
    public List<HomeBannerVO> listActive() {
        return homeBannerMapper.selectActiveList().stream().map(this::toVO).toList();
    }

    @Override
    public List<HomeBannerVO> listAdmin() {
        return homeBannerMapper.selectAdminList().stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HomeBannerVO create(HomeBannerSaveDTO dto) {
        HomeBannerEntity entity = new HomeBannerEntity();
        entity.setTitle(dto.getTitle().trim());
        entity.setSubtitle(dto.getSubtitle().trim());
        entity.setButtonText(dto.getButtonText());
        entity.setTargetUrl(dto.getTargetUrl());
        entity.setImageUrl(dto.getImageUrl());
        entity.setSortOrder(dto.getSortOrder());
        entity.setStatus(1);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        homeBannerMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HomeBannerVO update(Long bannerId, HomeBannerSaveDTO dto) {
        HomeBannerEntity entity = getBannerOrThrow(bannerId);
        entity.setTitle(dto.getTitle().trim());
        entity.setSubtitle(dto.getSubtitle().trim());
        entity.setButtonText(dto.getButtonText());
        entity.setTargetUrl(dto.getTargetUrl());
        entity.setImageUrl(dto.getImageUrl());
        entity.setSortOrder(dto.getSortOrder());
        entity.setUpdatedAt(LocalDateTime.now());
        homeBannerMapper.update(entity);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO updateStatus(Long bannerId, HomeBannerStatusDTO dto) {
        getBannerOrThrow(bannerId);
        if (!Objects.equals(dto.getStatus(), 1) && !Objects.equals(dto.getStatus(), 2)) {
            throw new BizException(400, "状态不合法");
        }
        homeBannerMapper.updateStatus(bannerId, dto.getStatus());
        return AdminOperationVO.builder()
                .targetId(bannerId)
                .status(dto.getStatus())
                .message(Objects.equals(dto.getStatus(), 1) ? "轮播已启用" : "轮播已停用")
                .build();
    }

    @Override
    public HomeBannerEntity getActiveBanner(Long bannerId) {
        HomeBannerEntity entity = getBannerOrThrow(bannerId);
        if (!Objects.equals(entity.getStatus(), 1)) {
            throw new BizException(400, "所选轮播已停用");
        }
        return entity;
    }

    private HomeBannerEntity getBannerOrThrow(Long bannerId) {
        HomeBannerEntity entity = homeBannerMapper.selectById(bannerId);
        if (entity == null) {
            throw new BizException(404, "轮播不存在");
        }
        return entity;
    }

    private HomeBannerVO toVO(HomeBannerEntity entity) {
        return HomeBannerVO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .subtitle(entity.getSubtitle())
                .buttonText(entity.getButtonText())
                .targetUrl(entity.getTargetUrl())
                .imageUrl(entity.getImageUrl())
                .sortOrder(entity.getSortOrder())
                .status(entity.getStatus())
                .build();
    }
}
