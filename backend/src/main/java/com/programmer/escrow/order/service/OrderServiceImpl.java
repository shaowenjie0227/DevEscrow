package com.programmer.escrow.order.service;

import com.programmer.escrow.common.enums.OrderStatusEnum;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.demand.entity.DemandEntity;
import com.programmer.escrow.demand.mapper.DemandMapper;
import com.programmer.escrow.demand.model.DemandPayload;
import com.programmer.escrow.demand.model.DemandStagePlan;
import com.programmer.escrow.demand.util.DemandPayloadUtils;
import com.programmer.escrow.infra.sequence.BizNoGenerator;
import com.programmer.escrow.order.entity.OrderEntity;
import com.programmer.escrow.order.entity.OrderStageEntity;
import com.programmer.escrow.order.mapper.OrderMapper;
import com.programmer.escrow.order.mapper.OrderStageMapper;
import com.programmer.escrow.order.dto.OrderCreateDTO;
import com.programmer.escrow.order.dto.OrderPayDTO;
import com.programmer.escrow.order.dto.OrderRejectDTO;
import com.programmer.escrow.order.dto.OrderRemarkDTO;
import com.programmer.escrow.order.dto.OrderSubmitDTO;
import com.programmer.escrow.order.vo.OrderDetailVO;
import com.programmer.escrow.order.vo.OrderListVO;
import com.programmer.escrow.quote.entity.QuoteEntity;
import com.programmer.escrow.quote.mapper.QuoteMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.programmer.escrow.common.util.JsonArrayUtils.toJson;
import static com.programmer.escrow.common.util.JsonArrayUtils.toStringList;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderStageMapper orderStageMapper;
    private final QuoteMapper quoteMapper;
    private final DemandMapper demandMapper;
    private final OrderStateMachineService orderStateMachineService;
    private final BizNoGenerator bizNoGenerator;

    public OrderServiceImpl(OrderMapper orderMapper,
                            OrderStageMapper orderStageMapper,
                            QuoteMapper quoteMapper,
                            DemandMapper demandMapper,
                            OrderStateMachineService orderStateMachineService,
                            BizNoGenerator bizNoGenerator) {
        this.orderMapper = orderMapper;
        this.orderStageMapper = orderStageMapper;
        this.quoteMapper = quoteMapper;
        this.demandMapper = demandMapper;
        this.orderStateMachineService = orderStateMachineService;
        this.bizNoGenerator = bizNoGenerator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO createOrder(Long clientId, OrderCreateDTO dto) {
        DemandEntity demand = demandMapper.selectById(dto.getDemandId());
        if (demand == null) {
            throw new BizException(404, "需求不存在");
        }
        if (!Objects.equals(demand.getPublisherId(), clientId)) {
            throw new BizException(403, "你无权为该需求创建订单");
        }
        if (!Objects.equals(demand.getReviewStatus(), 1) || !(Objects.equals(demand.getStatus(), 2) || Objects.equals(demand.getStatus(), 3))) {
            throw new BizException(400, "该需求当前不能创建订单");
        }
        if (orderMapper.selectByDemandId(dto.getDemandId()) != null) {
            throw new BizException(400, "该需求已生成订单");
        }
        QuoteEntity quote = quoteMapper.selectById(dto.getQuoteId());
        if (quote == null || !Objects.equals(quote.getDemandId(), dto.getDemandId()) || !Objects.equals(quote.getStatus(), 0)) {
            throw new BizException(400, "报价无效");
        }

        OrderEntity entity = new OrderEntity();
        entity.setOrderNo(bizNoGenerator.nextOrderNo());
        entity.setDemandId(dto.getDemandId());
        entity.setQuoteId(dto.getQuoteId());
        entity.setClientId(clientId);
        entity.setDeveloperId(quote.getDeveloperId());
        entity.setOrderTitle(demand.getTitle());
        entity.setAmountTotal(quote.getPriceTotal());
        entity.setEscrowAmount(quote.getPriceTotal());
        entity.setPlatformFee(BigDecimal.ZERO);
        entity.setPayStatus(0);
        entity.setStatus(OrderStatusEnum.QUOTED.getCode());
        entity.setCurrentStageNo(1);
        entity.setProgressPercent(0);
        entity.setSelectedAt(LocalDateTime.now());
        entity.setVersion(0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        orderMapper.insert(entity);
        buildOrderStages(entity, demand).forEach(orderStageMapper::insert);

        quoteMapper.updateSelected(quote.getId(), LocalDateTime.now());
        quoteMapper.rejectOtherQuotes(dto.getDemandId(), quote.getId());
        demandMapper.updateStatus(dto.getDemandId(), 4);
        return toDetailVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO payOrder(Long clientId, Long orderId, OrderPayDTO dto) {
        OrderEntity order = getOwnedClientOrder(clientId, orderId);
        orderStateMachineService.assertCanTransfer(OrderStatusEnum.QUOTED, OrderStatusEnum.PAID);
        int updated = orderMapper.updateToPaid(orderId, clientId, LocalDateTime.now());
        if (updated == 0) {
            throw new BizException(400, "当前订单状态不允许托管支付");
        }
        OrderEntity latest = orderMapper.selectById(orderId);
        return toDetailVO(latest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO acceptOrder(Long clientId, Long orderId, OrderRemarkDTO dto) {
        OrderEntity order = getOwnedClientOrder(clientId, orderId);
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.WAIT_ACCEPT.getCode())) {
            throw new BizException(400, "当前订单不是待验收状态");
        }
        int stageCount = orderStageMapper.countByOrderId(orderId);
        int currentStageNo = order.getCurrentStageNo();
        LocalDateTime now = LocalDateTime.now();
        int stageUpdated = orderStageMapper.updateToAccepted(orderId, currentStageNo, now);
        if (stageUpdated == 0) {
            throw new BizException(400, "当前阶段状态不允许验收通过");
        }
        if (currentStageNo >= stageCount) {
            orderStateMachineService.assertCanTransfer(OrderStatusEnum.WAIT_ACCEPT, OrderStatusEnum.COMPLETED);
            int updated = orderMapper.updateToCompleted(orderId, clientId, currentStageNo, now, now);
            if (updated == 0) {
                throw new BizException(400, "当前订单状态不允许验收通过");
            }
            demandMapper.updateStatus(order.getDemandId(), 6);
        } else {
            int nextStageNo = currentStageNo + 1;
            int updated = orderMapper.advanceToNextStage(
                    orderId,
                    clientId,
                    currentStageNo,
                    nextStageNo,
                    calculateStartedProgress(nextStageNo, stageCount)
            );
            if (updated == 0) {
                throw new BizException(400, "当前订单状态不允许进入下一阶段");
            }
            orderStageMapper.updateToInProgress(orderId, nextStageNo, now);
            demandMapper.updateStatus(order.getDemandId(), 5);
        }
        return toDetailVO(orderMapper.selectById(orderId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO rejectOrder(Long clientId, Long orderId, OrderRejectDTO dto) {
        OrderEntity order = getOwnedClientOrder(clientId, orderId);
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.WAIT_ACCEPT.getCode())) {
            throw new BizException(400, "当前订单不是待验收状态");
        }
        orderStateMachineService.assertCanTransfer(OrderStatusEnum.WAIT_ACCEPT, OrderStatusEnum.IN_PROGRESS);
        int stageCount = orderStageMapper.countByOrderId(orderId);
        int currentStageNo = order.getCurrentStageNo();
        int updated = orderMapper.updateToRejected(
                orderId,
                clientId,
                currentStageNo,
                calculateStartedProgress(currentStageNo, stageCount)
        );
        if (updated == 0) {
            throw new BizException(400, "当前订单状态不允许打回");
        }
        orderStageMapper.updateToRejected(orderId, currentStageNo, dto.getReason());
        demandMapper.updateStatus(order.getDemandId(), 5);
        return toDetailVO(orderMapper.selectById(orderId));
    }

    @Override
    public OrderDetailVO developerAccept(Long developerId, Long orderId, OrderRemarkDTO dto) {
        OrderEntity order = getOwnedDeveloperOrder(developerId, orderId);
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.PAID.getCode())) {
            throw new BizException(400, "当前订单尚未进入可接单状态");
        }
        return toDetailVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO developerStart(Long developerId, Long orderId, OrderRemarkDTO dto) {
        OrderEntity order = getOwnedDeveloperOrder(developerId, orderId);
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.PAID.getCode())) {
            throw new BizException(400, "当前订单尚不能开始开发");
        }
        orderStateMachineService.assertCanTransfer(OrderStatusEnum.PAID, OrderStatusEnum.IN_PROGRESS);
        int updated = orderMapper.updateToInProgress(orderId, developerId, LocalDateTime.now());
        if (updated == 0) {
            throw new BizException(400, "当前订单状态不允许开始开发");
        }
        orderStageMapper.updateToInProgress(orderId, order.getCurrentStageNo(), LocalDateTime.now());
        demandMapper.updateStatus(order.getDemandId(), 5);
        return toDetailVO(orderMapper.selectById(orderId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO developerSubmit(Long developerId, Long orderId, OrderSubmitDTO dto) {
        OrderEntity order = getOwnedDeveloperOrder(developerId, orderId);
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.IN_PROGRESS.getCode())) {
            throw new BizException(400, "当前订单不是开发中状态");
        }
        orderStateMachineService.assertCanTransfer(OrderStatusEnum.IN_PROGRESS, OrderStatusEnum.WAIT_ACCEPT);
        int stageCount = orderStageMapper.countByOrderId(orderId);
        int currentStageNo = order.getCurrentStageNo();
        LocalDateTime now = LocalDateTime.now();
        int updated = orderMapper.updateToWaitAccept(
                orderId,
                developerId,
                currentStageNo,
                now,
                calculateSubmittedProgress(currentStageNo, stageCount)
        );
        if (updated == 0) {
            throw new BizException(400, "当前订单状态不允许提交交付");
        }
        orderStageMapper.updateToSubmitted(orderId, currentStageNo, dto.getSubmitContent(), toJson(dto.getDeliverables()), now);
        return toDetailVO(orderMapper.selectById(orderId));
    }

    @Override
    public List<OrderListVO> listClientOrders(Long clientId) {
        return orderMapper.selectByClientId(clientId).stream()
                .map(this::toListVO)
                .toList();
    }

    @Override
    public List<OrderListVO> listDeveloperOrders(Long developerId) {
        return orderMapper.selectByDeveloperId(developerId).stream()
                .map(this::toListVO)
                .toList();
    }

    @Override
    public OrderDetailVO getClientOrderDetail(Long clientId, Long orderId) {
        return toDetailVO(getOwnedClientOrder(clientId, orderId));
    }

    @Override
    public OrderDetailVO getDeveloperOrderDetail(Long developerId, Long orderId) {
        return toDetailVO(getOwnedDeveloperOrder(developerId, orderId));
    }

    private OrderListVO toListVO(OrderEntity entity) {
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

    private OrderDetailVO toDetailVO(OrderEntity entity) {
        OrderStageEntity currentStage = orderStageMapper.selectByOrderIdAndStageNo(entity.getId(), entity.getCurrentStageNo());
        int stageCount = orderStageMapper.countByOrderId(entity.getId());
        return OrderDetailVO.builder()
                .id(entity.getId())
                .orderNo(entity.getOrderNo())
                .demandId(entity.getDemandId())
                .quoteId(entity.getQuoteId())
                .clientId(entity.getClientId())
                .developerId(entity.getDeveloperId())
                .orderTitle(entity.getOrderTitle())
                .amountTotal(entity.getAmountTotal())
                .escrowAmount(entity.getEscrowAmount())
                .payStatus(entity.getPayStatus())
                .status(entity.getStatus())
                .currentStageNo(entity.getCurrentStageNo())
                .stageCount(stageCount)
                .progressPercent(entity.getProgressPercent())
                .deliverables(currentStage == null ? List.of() : toStringList(currentStage.getDeliverableJson()))
                .build();
    }

    private OrderEntity getOwnedClientOrder(Long clientId, Long orderId) {
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        if (!Objects.equals(order.getClientId(), clientId)) {
            throw new BizException(403, "你无权操作该订单");
        }
        return order;
    }

    private OrderEntity getOwnedDeveloperOrder(Long developerId, Long orderId) {
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        if (!Objects.equals(order.getDeveloperId(), developerId)) {
            throw new BizException(403, "你无权操作该订单");
        }
        return order;
    }

    private List<OrderStageEntity> buildOrderStages(OrderEntity order, DemandEntity demand) {
        DemandPayload payload = DemandPayloadUtils.parse(demand.getAttachmentsJson());
        List<DemandStagePlan> stagePlans = payload.getStagePlans();
        if (!Objects.equals(demand.getDeliveryType(), 2) || stagePlans == null || stagePlans.isEmpty()) {
            return List.of(buildStage(order, 1, "第1阶段", "单阶段交付", order.getAmountTotal()));
        }

        BigDecimal plannedTotal = stagePlans.stream()
                .map(DemandStagePlan::getStageAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (plannedTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of(buildStage(order, 1, "第1阶段", "单阶段交付", order.getAmountTotal()));
        }

        List<OrderStageEntity> stages = new ArrayList<>();
        BigDecimal allocated = BigDecimal.ZERO;
        for (int index = 0; index < stagePlans.size(); index++) {
            DemandStagePlan stagePlan = stagePlans.get(index);
            BigDecimal stageAmount;
            if (index == stagePlans.size() - 1) {
                stageAmount = order.getAmountTotal().subtract(allocated);
            } else {
                stageAmount = order.getAmountTotal()
                        .multiply(stagePlan.getStageAmount())
                        .divide(plannedTotal, 2, RoundingMode.HALF_UP);
                allocated = allocated.add(stageAmount);
            }
            stages.add(buildStage(
                    order,
                    index + 1,
                    stagePlan.getStageName() == null || stagePlan.getStageName().isBlank() ? "第" + (index + 1) + "阶段" : stagePlan.getStageName(),
                    stagePlan.getStageDesc(),
                    stageAmount
            ));
        }
        return stages;
    }

    private OrderStageEntity buildStage(OrderEntity order,
                                        int stageNo,
                                        String stageName,
                                        String stageDesc,
                                        BigDecimal stageAmount) {
        OrderStageEntity stage = new OrderStageEntity();
        stage.setOrderId(order.getId());
        stage.setStageNo(stageNo);
        stage.setStageName(stageName);
        stage.setStageDesc(stageDesc);
        stage.setStageAmount(stageAmount);
        stage.setStatus(0);
        stage.setVersion(0);
        stage.setCreatedAt(LocalDateTime.now());
        stage.setUpdatedAt(LocalDateTime.now());
        return stage;
    }

    private int calculateStartedProgress(int currentStageNo, int stageCount) {
        if (stageCount <= 1) {
            return 5;
        }
        double completedRatio = (double) (currentStageNo - 1) / stageCount;
        return Math.max(5, Math.min(95, (int) Math.floor(completedRatio * 100 + 5)));
    }

    private int calculateSubmittedProgress(int currentStageNo, int stageCount) {
        if (stageCount <= 1 || currentStageNo >= stageCount) {
            return 90;
        }
        int minProgress = calculateStartedProgress(currentStageNo, stageCount) + 1;
        int calculated = (int) Math.floor(((double) currentStageNo / stageCount) * 100 - 5);
        return Math.max(minProgress, Math.min(95, calculated));
    }
}
