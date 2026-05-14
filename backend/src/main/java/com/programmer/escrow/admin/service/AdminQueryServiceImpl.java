package com.programmer.escrow.admin.service;

import com.programmer.escrow.admin.vo.AdminUserVO;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.common.util.JsonArrayUtils;
import com.programmer.escrow.demand.entity.DemandEntity;
import com.programmer.escrow.demand.mapper.DemandMapper;
import com.programmer.escrow.demand.model.DemandFileItem;
import com.programmer.escrow.demand.model.DemandPayload;
import com.programmer.escrow.demand.model.DemandStagePlan;
import com.programmer.escrow.demand.util.DemandPayloadUtils;
import com.programmer.escrow.demand.vo.DemandDetailVO;
import com.programmer.escrow.order.entity.OrderEntity;
import com.programmer.escrow.order.entity.OrderStageEntity;
import com.programmer.escrow.order.mapper.OrderMapper;
import com.programmer.escrow.order.mapper.OrderStageMapper;
import com.programmer.escrow.order.vo.OrderDetailVO;
import com.programmer.escrow.order.vo.OrderListVO;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminQueryServiceImpl implements AdminQueryService {

    private final UserMapper userMapper;
    private final DemandMapper demandMapper;
    private final OrderMapper orderMapper;
    private final OrderStageMapper orderStageMapper;

    public AdminQueryServiceImpl(UserMapper userMapper,
                                 DemandMapper demandMapper,
                                 OrderMapper orderMapper,
                                 OrderStageMapper orderStageMapper) {
        this.userMapper = userMapper;
        this.demandMapper = demandMapper;
        this.orderMapper = orderMapper;
        this.orderStageMapper = orderStageMapper;
    }

    @Override
    public List<AdminUserVO> listUsers(Integer status, Integer userType, String keyword) {
        return userMapper.selectAdminList(status, userType, keyword).stream()
                .map(this::toUserVO)
                .toList();
    }

    @Override
    public List<DemandDetailVO> listDemands(Integer reviewStatus, Integer status) {
        return demandMapper.selectAdminList(reviewStatus, status).stream()
                .map(this::toDemandVO)
                .toList();
    }

    @Override
    public List<OrderListVO> listOrders(Integer status, String keyword) {
        return orderMapper.selectAdminList(status, keyword).stream()
                .map(this::toOrderListVO)
                .toList();
    }

    @Override
    public OrderDetailVO getOrderDetail(Long orderId) {
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        OrderStageEntity stage = orderStageMapper.selectByOrderIdAndStageNo(orderId, order.getCurrentStageNo());
        return OrderDetailVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .demandId(order.getDemandId())
                .quoteId(order.getQuoteId())
                .clientId(order.getClientId())
                .developerId(order.getDeveloperId())
                .orderTitle(order.getOrderTitle())
                .amountTotal(order.getAmountTotal())
                .escrowAmount(order.getEscrowAmount())
                .payStatus(order.getPayStatus())
                .status(order.getStatus())
                .currentStageNo(order.getCurrentStageNo())
                .stageCount(orderStageMapper.countByOrderId(orderId))
                .progressPercent(order.getProgressPercent())
                .deliverables(stage == null ? List.of() : JsonArrayUtils.toStringList(stage.getDeliverableJson()))
                .build();
    }

    private AdminUserVO toUserVO(UserEntity entity) {
        return AdminUserVO.builder()
                .id(entity.getId())
                .userNo(entity.getUserNo())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .nickname(entity.getNickname())
                .userType(entity.getUserType())
                .developerStatus(entity.getDeveloperStatus())
                .idVerifyStatus(entity.getIdVerifyStatus())
                .skillAuditStatus(entity.getSkillAuditStatus())
                .developerRoleType(entity.getDeveloperRoleType())
                .skillTags(entity.getSkillTags())
                .realName(entity.getRealName())
                .idCardFrontUrl(entity.getIdCardFrontUrl())
                .idCardBackUrl(entity.getIdCardBackUrl())
                .selfieUrl(entity.getSelfieUrl())
                .status(entity.getStatus())
                .build();
    }

    private DemandDetailVO toDemandVO(DemandEntity entity) {
        DemandPayload payload = DemandPayloadUtils.parse(entity.getAttachmentsJson());
        List<DemandFileItem> images = sanitizeFiles(payload.getImages());
        List<DemandFileItem> attachments = sanitizeFiles(payload.getAttachments());
        List<DemandStagePlan> stagePlans = sanitizeStages(payload.getStagePlans());
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

    private OrderListVO toOrderListVO(OrderEntity entity) {
        return OrderListVO.builder()
                .id(entity.getId())
                .orderNo(entity.getOrderNo())
                .orderTitle(entity.getOrderTitle())
                .amountTotal(entity.getAmountTotal())
                .status(entity.getStatus())
                .payStatus(entity.getPayStatus())
                .progressPercent(entity.getProgressPercent())
                .build();
    }

    private List<DemandFileItem> sanitizeFiles(List<DemandFileItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<DemandFileItem> results = new ArrayList<>();
        for (DemandFileItem item : items) {
            if (item == null || item.getUrl() == null || item.getUrl().isBlank()) {
                continue;
            }
            results.add(item);
        }
        return results;
    }

    private List<DemandStagePlan> sanitizeStages(List<DemandStagePlan> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<DemandStagePlan> results = new ArrayList<>();
        for (DemandStagePlan item : items) {
            if (item == null || item.getStageDesc() == null || item.getStageDesc().isBlank() || item.getStageAmount() == null) {
                continue;
            }
            results.add(item);
        }
        return results;
    }
}
