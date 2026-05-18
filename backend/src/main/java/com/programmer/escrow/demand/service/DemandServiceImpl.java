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
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DemandServiceImpl implements DemandService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final DemandMapper demandMapper;
    private final BizNoGenerator bizNoGenerator;
    private final DemandCategoryService demandCategoryService;
    private final UserMapper userMapper;

    public DemandServiceImpl(DemandMapper demandMapper,
                             BizNoGenerator bizNoGenerator,
                             DemandCategoryService demandCategoryService,
                             UserMapper userMapper) {
        this.demandMapper = demandMapper;
        this.bizNoGenerator = bizNoGenerator;
        this.demandCategoryService = demandCategoryService;
        this.userMapper = userMapper;
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
    public DemandDetailVO getMyDemandDetail(Long publisherId, Long demandId) {
        DemandEntity entity = getDemandOrThrow(demandId);
        if (!Objects.equals(entity.getPublisherId(), publisherId)) {
            throw new BizException(403, "you are not allowed to view this demand");
        }
        return toDetailVO(entity);
    }

    @Override
    public DemandDetailVO getMarketDemandDetail(Long demandId) {
        DemandEntity entity = getDemandOrThrow(demandId);
        if (!isMarketVisible(entity)) {
            throw new BizException(404, "demand does not exist");
        }
        return toDetailVO(entity);
    }

    private DemandEntity getDemandOrThrow(Long demandId) {
        DemandEntity entity = demandMapper.selectById(demandId);
        if (entity == null) {
            throw new BizException(404, "demand does not exist");
        }
        return entity;
    }

    private DemandDetailVO toDetailVO(DemandEntity entity) {
        DemandPayload payload = DemandPayloadUtils.parse(entity.getAttachmentsJson());
        List<DemandFileItem> images = safeFiles(payload.getImages());
        List<DemandFileItem> attachments = safeFiles(payload.getAttachments());
        List<DemandStagePlan> stagePlans = safeStagePlans(payload.getStagePlans());
        boolean urgent = Boolean.TRUE.equals(payload.getUrgent());
        UserEntity publisher = userMapper.selectById(entity.getPublisherId());
        return DemandDetailVO.builder()
                .id(entity.getId())
                .demandNo(entity.getDemandNo())
                .publisherId(entity.getPublisherId())
                .publisherNickname(publisher == null ? null : publisher.getNickname())
                .publisherAvatarUrl(publisher == null ? null : publisher.getAvatarUrl())
                .title(entity.getTitle())
                .summary(entity.getSummary())
                .detail(entity.getDetail())
                .categoryId(entity.getCategoryId())
                .category(entity.getCategory())
                .orderType(normalizeOrderType(payload.getOrderType()))
                .urgent(urgent)
                .urgentBonus(normalizeUrgentBonus(urgent, payload.getUrgentBonus()))
                .budgetMin(entity.getBudgetMin())
                .budgetMax(entity.getBudgetMax())
                .expectedDays(entity.getExpectedDays())
                .deliveryType(entity.getDeliveryType())
                .quoteCount(entity.getQuoteCount())
                .reviewStatus(entity.getReviewStatus())
                .status(entity.getStatus())
                .rejectReason(entity.getRejectReason())
                .coverImage(images.isEmpty() ? null : images.get(0).getUrl())
                .images(images)
                .attachments(attachments)
                .stagePlans(stagePlans)
                .build();
    }

    private void validateDemand(DemandCreateDTO dto) {
        if (dto.getBudgetMax().compareTo(dto.getBudgetMin()) < 0) {
            throw new BizException(400, "budgetMax cannot be less than budgetMin");
        }
        if (dto.getExpectedDays() == null || dto.getExpectedDays() <= 0) {
            throw new BizException(400, "expectedDays must be greater than 0");
        }
        if (!Objects.equals(dto.getDeliveryType(), 1) && !Objects.equals(dto.getDeliveryType(), 2)) {
            throw new BizException(400, "deliveryType is invalid");
        }
        if (dto.getCategoryId() == null) {
            throw new BizException(400, "categoryId is required");
        }
        if (dto.getOrderType() != null && !Objects.equals(dto.getOrderType(), 1) && !Objects.equals(dto.getOrderType(), 2)) {
            throw new BizException(400, "orderType is invalid");
        }
        if (dto.getUrgentBonus() != null && dto.getUrgentBonus().compareTo(ZERO) < 0) {
            throw new BizException(400, "urgentBonus cannot be negative");
        }

        boolean urgent = Boolean.TRUE.equals(dto.getUrgent());
        if (urgent && normalizeUrgentBonus(true, dto.getUrgentBonus()).compareTo(ZERO) <= 0) {
            throw new BizException(400, "urgentBonus is required when urgent is true");
        }

        if (Objects.equals(dto.getDeliveryType(), 2)) {
            List<DemandStagePlan> stagePlans = safeStagePlans(dto.getStagePlans());
            if (stagePlans.size() < 2) {
                throw new BizException(400, "multi-stage delivery requires at least two valid stages");
            }
            BigDecimal stageTotal = stagePlans.stream()
                    .map(DemandStagePlan::getStageAmount)
                    .reduce(ZERO, BigDecimal::add);
            if (stageTotal.compareTo(dto.getBudgetMin()) < 0 || stageTotal.compareTo(dto.getBudgetMax()) > 0) {
                throw new BizException(400, "stage total must stay within the budget range");
            }
        }
    }

    private DemandPayload buildPayload(DemandCreateDTO dto) {
        boolean urgent = Boolean.TRUE.equals(dto.getUrgent());
        DemandPayload payload = new DemandPayload();
        payload.setOrderType(normalizeOrderType(dto.getOrderType()));
        payload.setUrgent(urgent);
        payload.setUrgentBonus(normalizeUrgentBonus(urgent, dto.getUrgentBonus()));
        payload.setImages(safeFiles(dto.getImages()));
        payload.setAttachments(safeFiles(dto.getAttachments()));
        payload.setStagePlans(Objects.equals(dto.getDeliveryType(), 2) ? safeStagePlans(dto.getStagePlans()) : List.of());
        return payload;
    }

    private Integer normalizeOrderType(Integer orderType) {
        return Objects.equals(orderType, 2) ? 2 : 1;
    }

    private BigDecimal normalizeUrgentBonus(boolean urgent, BigDecimal urgentBonus) {
        if (!urgent || urgentBonus == null) {
            return ZERO;
        }
        return urgentBonus;
    }

    private boolean isMarketVisible(DemandEntity entity) {
        return Objects.equals(entity.getReviewStatus(), 1)
                && (Objects.equals(entity.getStatus(), 2) || Objects.equals(entity.getStatus(), 3));
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
            normalized.setName(item.getName() == null || item.getName().isBlank() ? "Unnamed file" : item.getName().trim());
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
            if (item == null
                    || item.getStageDesc() == null
                    || item.getStageDesc().isBlank()
                    || item.getStageAmount() == null
                    || item.getStageAmount().compareTo(ZERO) <= 0) {
                continue;
            }
            DemandStagePlan normalized = new DemandStagePlan();
            normalized.setStageNo(index + 1);
            normalized.setStageName(item.getStageName() == null || item.getStageName().isBlank()
                    ? "Stage " + (index + 1)
                    : item.getStageName().trim());
            normalized.setStageDesc(item.getStageDesc().trim());
            normalized.setStageAmount(item.getStageAmount());
            results.add(normalized);
        }
        return results;
    }
}
