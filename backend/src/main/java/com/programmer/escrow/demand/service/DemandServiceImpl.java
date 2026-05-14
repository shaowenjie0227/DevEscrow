package com.programmer.escrow.demand.service;

import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.demand.dto.DemandCreateDTO;
import com.programmer.escrow.demand.entity.DemandCategoryEntity;
import com.programmer.escrow.demand.entity.DemandEntity;
import com.programmer.escrow.demand.mapper.DemandMapper;
import com.programmer.escrow.demand.model.DemandFileItem;
import com.programmer.escrow.demand.model.DemandPayload;
import com.programmer.escrow.demand.model.DemandStagePlan;
import com.programmer.escrow.demand.util.DemandPayloadUtils;
import com.programmer.escrow.demand.vo.DemandDetailVO;
import com.programmer.escrow.infra.sequence.BizNoGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DemandServiceImpl implements DemandService {

    private final DemandMapper demandMapper;
    private final BizNoGenerator bizNoGenerator;
    private final DemandCategoryService demandCategoryService;

    public DemandServiceImpl(DemandMapper demandMapper,
                             BizNoGenerator bizNoGenerator,
                             DemandCategoryService demandCategoryService) {
        this.demandMapper = demandMapper;
        this.bizNoGenerator = bizNoGenerator;
        this.demandCategoryService = demandCategoryService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DemandDetailVO createDemand(Long publisherId, DemandCreateDTO dto) {
        validateDemand(dto);
        DemandCategoryEntity category = demandCategoryService.getEnabledCategory(dto.getCategoryId());
        DemandPayload payload = buildPayload(dto);

        DemandEntity entity = new DemandEntity();
        entity.setDemandNo(bizNoGenerator.nextDemandNo());
        entity.setPublisherId(publisherId);
        entity.setTitle(dto.getTitle());
        entity.setSummary(dto.getSummary());
        entity.setDetail(dto.getDetail());
        entity.setCategoryId(category.getId());
        entity.setCategory(category.getCategoryName());
        entity.setBudgetMin(dto.getBudgetMin());
        entity.setBudgetMax(dto.getBudgetMax());
        entity.setExpectedDays(dto.getExpectedDays());
        entity.setDeliveryType(dto.getDeliveryType());
        entity.setAttachmentsJson(DemandPayloadUtils.toJson(payload));
        entity.setQuoteCount(0);
        entity.setReviewStatus(0);
        entity.setStatus(1);
        entity.setVersion(0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        demandMapper.insert(entity);
        return toDetailVO(entity);
    }

    @Override
    public List<DemandDetailVO> listMyDemands(Long publisherId) {
        return demandMapper.selectByPublisherId(publisherId).stream()
                .map(this::toDetailVO)
                .toList();
    }

    @Override
    public List<DemandDetailVO> listMarketDemands() {
        return demandMapper.selectMarketDemands().stream()
                .map(this::toDetailVO)
                .toList();
    }

    @Override
    public DemandDetailVO getDemandDetail(Long demandId) {
        DemandEntity entity = demandMapper.selectById(demandId);
        if (entity == null) {
            throw new BizException(404, "需求不存在");
        }
        return toDetailVO(entity);
    }

    private DemandDetailVO toDetailVO(DemandEntity entity) {
        DemandPayload payload = DemandPayloadUtils.parse(entity.getAttachmentsJson());
        List<DemandFileItem> images = safeFiles(payload.getImages());
        List<DemandFileItem> attachments = safeFiles(payload.getAttachments());
        List<DemandStagePlan> stagePlans = safeStagePlans(payload.getStagePlans());
        return DemandDetailVO.builder()
                .id(entity.getId())
                .demandNo(entity.getDemandNo())
                .title(entity.getTitle())
                .summary(entity.getSummary())
                .detail(entity.getDetail())
                .categoryId(entity.getCategoryId())
                .category(entity.getCategory())
                .budgetMin(entity.getBudgetMin())
                .budgetMax(entity.getBudgetMax())
                .expectedDays(entity.getExpectedDays())
                .deliveryType(entity.getDeliveryType())
                .reviewStatus(entity.getReviewStatus())
                .status(entity.getStatus())
                .coverImage(images.isEmpty() ? null : images.get(0).getUrl())
                .images(images)
                .attachments(attachments)
                .stagePlans(stagePlans)
                .build();
    }

    private void validateDemand(DemandCreateDTO dto) {
        if (dto.getBudgetMax().compareTo(dto.getBudgetMin()) < 0) {
            throw new BizException(400, "预算上限不能小于预算下限");
        }
        if (dto.getExpectedDays() == null || dto.getExpectedDays() <= 0) {
            throw new BizException(400, "预计工期必须大于0");
        }
        if (!Objects.equals(dto.getDeliveryType(), 1) && !Objects.equals(dto.getDeliveryType(), 2)) {
            throw new BizException(400, "交付类型不合法");
        }
        if (dto.getCategoryId() == null) {
            throw new BizException(400, "分类不能为空");
        }
        if (Objects.equals(dto.getDeliveryType(), 2)) {
            List<DemandStagePlan> stagePlans = safeStagePlans(dto.getStagePlans());
            if (stagePlans.size() < 2) {
                throw new BizException(400, "多阶段交付至少需要两个阶段");
            }
            BigDecimal stageTotal = stagePlans.stream()
                    .map(DemandStagePlan::getStageAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (stageTotal.compareTo(dto.getBudgetMin()) < 0 || stageTotal.compareTo(dto.getBudgetMax()) > 0) {
                throw new BizException(400, "阶段结算总额需落在整体预算区间内");
            }
        }
    }

    private DemandPayload buildPayload(DemandCreateDTO dto) {
        DemandPayload payload = new DemandPayload();
        payload.setImages(safeFiles(dto.getImages()));
        payload.setAttachments(safeFiles(dto.getAttachments()));
        payload.setStagePlans(Objects.equals(dto.getDeliveryType(), 2) ? safeStagePlans(dto.getStagePlans()) : List.of());
        return payload;
    }

    private List<DemandFileItem> safeFiles(List<DemandFileItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<DemandFileItem> results = new ArrayList<>();
        for (DemandFileItem item : items) {
            if (item == null || item.getUrl() == null || item.getUrl().isBlank()) {
                continue;
            }
            DemandFileItem normalized = new DemandFileItem();
            normalized.setName(item.getName() == null || item.getName().isBlank() ? "未命名文件" : item.getName().trim());
            normalized.setUrl(item.getUrl().trim());
            normalized.setContentType(item.getContentType());
            normalized.setSize(item.getSize());
            results.add(normalized);
        }
        return results;
    }

    private List<DemandStagePlan> safeStagePlans(List<DemandStagePlan> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<DemandStagePlan> results = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            DemandStagePlan item = items.get(index);
            if (item == null || item.getStageDesc() == null || item.getStageDesc().isBlank() || item.getStageAmount() == null) {
                continue;
            }
            DemandStagePlan normalized = new DemandStagePlan();
            normalized.setStageNo(index + 1);
            normalized.setStageName("第" + (index + 1) + "阶段");
            normalized.setStageDesc(item.getStageDesc().trim());
            normalized.setStageAmount(item.getStageAmount());
            results.add(normalized);
        }
        return results;
    }
}
