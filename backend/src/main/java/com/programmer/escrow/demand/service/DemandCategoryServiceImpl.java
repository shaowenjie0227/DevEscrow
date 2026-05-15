package com.programmer.escrow.demand.service;

import com.programmer.escrow.admin.dto.DemandCategorySaveDTO;
import com.programmer.escrow.admin.dto.DemandCategoryStatusDTO;
import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.demand.entity.DemandCategoryEntity;
import com.programmer.escrow.demand.mapper.DemandCategoryMapper;
import com.programmer.escrow.demand.mapper.DemandMapper;
import com.programmer.escrow.demand.vo.DemandCategoryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class DemandCategoryServiceImpl implements DemandCategoryService {

    private final DemandCategoryMapper demandCategoryMapper;
    private final DemandMapper demandMapper;

    public DemandCategoryServiceImpl(DemandCategoryMapper demandCategoryMapper, DemandMapper demandMapper) {
        this.demandCategoryMapper = demandCategoryMapper;
        this.demandMapper = demandMapper;
    }

    @Override
    public List<DemandCategoryVO> listActiveCategories() {
        return demandCategoryMapper.selectActiveList().stream().map(this::toVO).toList();
    }

    @Override
    public List<DemandCategoryVO> listAdminCategories() {
        return demandCategoryMapper.selectAdminList().stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DemandCategoryVO createCategory(DemandCategorySaveDTO dto) {
        String categoryName = normalize(dto.getCategoryName());
        ensureUnique(categoryName, null);

        DemandCategoryEntity entity = new DemandCategoryEntity();
        entity.setCategoryName(categoryName);
        entity.setSortOrder(dto.getSortOrder());
        entity.setStatus(1);
        entity.setDescription(normalize(dto.getDescription()));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        demandCategoryMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DemandCategoryVO updateCategory(Long categoryId, DemandCategorySaveDTO dto) {
        DemandCategoryEntity entity = getCategoryOrThrow(categoryId);
        String categoryName = normalize(dto.getCategoryName());
        ensureUnique(categoryName, categoryId);

        entity.setCategoryName(categoryName);
        entity.setSortOrder(dto.getSortOrder());
        entity.setDescription(normalize(dto.getDescription()));
        entity.setUpdatedAt(LocalDateTime.now());
        demandCategoryMapper.update(entity);
        demandMapper.updateCategorySnapshot(categoryId, categoryName);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO updateCategoryStatus(Long categoryId, DemandCategoryStatusDTO dto) {
        getCategoryOrThrow(categoryId);
        if (!Objects.equals(dto.getStatus(), 1) && !Objects.equals(dto.getStatus(), 2)) {
            throw new BizException(400, "分类状态不合法");
        }
        demandCategoryMapper.updateStatus(categoryId, dto.getStatus());
        return AdminOperationVO.builder()
                .targetId(categoryId)
                .status(dto.getStatus())
                .message(Objects.equals(dto.getStatus(), 1) ? "分类已启用" : "分类已停用")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO deleteCategory(Long categoryId) {
        getCategoryOrThrow(categoryId);
        if (demandMapper.countByCategoryId(categoryId) > 0) {
            throw new BizException(400, "该需求分类已被需求引用，暂时不能删除");
        }
        demandCategoryMapper.deleteById(categoryId);
        return AdminOperationVO.builder()
                .targetId(categoryId)
                .status(0)
                .message("需求分类已删除")
                .build();
    }

    @Override
    public DemandCategoryEntity getEnabledCategory(Long categoryId) {
        DemandCategoryEntity entity = getCategoryOrThrow(categoryId);
        if (!Objects.equals(entity.getStatus(), 1)) {
            throw new BizException(400, "所选分类已停用，请重新选择");
        }
        return entity;
    }

    private DemandCategoryEntity getCategoryOrThrow(Long categoryId) {
        DemandCategoryEntity entity = demandCategoryMapper.selectById(categoryId);
        if (entity == null) {
            throw new BizException(404, "需求分类不存在");
        }
        return entity;
    }

    private void ensureUnique(String categoryName, Long currentId) {
        DemandCategoryEntity existing = demandCategoryMapper.selectByName(categoryName);
        if (existing != null && !Objects.equals(existing.getId(), currentId)) {
            throw new BizException(400, "分类名称已存在");
        }
    }

    private String normalize(String text) {
        return text == null ? null : text.trim();
    }

    private DemandCategoryVO toVO(DemandCategoryEntity entity) {
        return DemandCategoryVO.builder()
                .id(entity.getId())
                .categoryName(entity.getCategoryName())
                .sortOrder(entity.getSortOrder())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .build();
    }
}
