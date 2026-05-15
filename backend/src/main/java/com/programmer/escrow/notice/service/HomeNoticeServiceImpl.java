package com.programmer.escrow.notice.service;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.notice.dto.HomeNoticeSaveDTO;
import com.programmer.escrow.notice.dto.HomeNoticeStatusDTO;
import com.programmer.escrow.notice.entity.HomeNoticeEntity;
import com.programmer.escrow.notice.mapper.HomeNoticeMapper;
import com.programmer.escrow.notice.vo.HomeNoticeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class HomeNoticeServiceImpl implements HomeNoticeService {

    private final HomeNoticeMapper homeNoticeMapper;

    public HomeNoticeServiceImpl(HomeNoticeMapper homeNoticeMapper) {
        this.homeNoticeMapper = homeNoticeMapper;
    }

    @Override
    public List<HomeNoticeVO> listActive() {
        return homeNoticeMapper.selectActiveList().stream().map(this::toVO).toList();
    }

    @Override
    public HomeNoticeVO getDetail(Long id) {
        return toVO(getActive(id));
    }

    @Override
    public List<HomeNoticeVO> listAdmin() {
        return homeNoticeMapper.selectAdminList().stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HomeNoticeVO create(HomeNoticeSaveDTO dto) {
        validateType(dto.getNoticeType());
        HomeNoticeEntity entity = new HomeNoticeEntity();
        entity.setNoticeType(dto.getNoticeType());
        entity.setTitle(dto.getTitle().trim());
        entity.setSummary(dto.getSummary().trim());
        entity.setTargetUrl(dto.getTargetUrl());
        entity.setCoverUrl(dto.getCoverUrl());
        entity.setSortOrder(dto.getSortOrder());
        entity.setStatus(1);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        homeNoticeMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HomeNoticeVO update(Long id, HomeNoticeSaveDTO dto) {
        validateType(dto.getNoticeType());
        HomeNoticeEntity entity = getOrThrow(id);
        entity.setNoticeType(dto.getNoticeType());
        entity.setTitle(dto.getTitle().trim());
        entity.setSummary(dto.getSummary().trim());
        entity.setTargetUrl(dto.getTargetUrl());
        entity.setCoverUrl(dto.getCoverUrl());
        entity.setSortOrder(dto.getSortOrder());
        entity.setUpdatedAt(LocalDateTime.now());
        homeNoticeMapper.update(entity);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO updateStatus(Long id, HomeNoticeStatusDTO dto) {
        getOrThrow(id);
        if (!Objects.equals(dto.getStatus(), 1) && !Objects.equals(dto.getStatus(), 2)) {
            throw new BizException(400, "状态不合法");
        }
        homeNoticeMapper.updateStatus(id, dto.getStatus());
        return AdminOperationVO.builder()
                .targetId(id)
                .status(dto.getStatus())
                .message(Objects.equals(dto.getStatus(), 1) ? "首页内容已启用" : "首页内容已停用")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO delete(Long id) {
        getOrThrow(id);
        homeNoticeMapper.deleteById(id);
        return AdminOperationVO.builder()
                .targetId(id)
                .status(0)
                .message("首页内容已删除")
                .build();
    }

    @Override
    public HomeNoticeEntity getActive(Long id) {
        HomeNoticeEntity entity = getOrThrow(id);
        if (!Objects.equals(entity.getStatus(), 1)) {
            throw new BizException(400, "首页内容已停用");
        }
        return entity;
    }

    private HomeNoticeEntity getOrThrow(Long id) {
        HomeNoticeEntity entity = homeNoticeMapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "首页内容不存在");
        }
        return entity;
    }

    private void validateType(Integer noticeType) {
        if (!Objects.equals(noticeType, 1) && !Objects.equals(noticeType, 2)) {
            throw new BizException(400, "内容类型不合法");
        }
    }

    private HomeNoticeVO toVO(HomeNoticeEntity entity) {
        return HomeNoticeVO.builder()
                .id(entity.getId())
                .noticeType(entity.getNoticeType())
                .typeLabel(resolveTypeLabel(entity.getNoticeType()))
                .title(entity.getTitle())
                .summary(entity.getSummary())
                .targetUrl(entity.getTargetUrl())
                .coverUrl(entity.getCoverUrl())
                .sortOrder(entity.getSortOrder())
                .status(entity.getStatus())
                .build();
    }

    private String resolveTypeLabel(Integer noticeType) {
        if (Objects.equals(noticeType, 2)) {
            return "活动";
        }
        return "公告";
    }
}
