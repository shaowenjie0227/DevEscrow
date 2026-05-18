package com.programmer.escrow.dispute.service;

import com.programmer.escrow.common.enums.OrderStatusEnum;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.common.util.JsonArrayUtils;
import com.programmer.escrow.demand.mapper.DemandMapper;
import com.programmer.escrow.dispute.dto.DisputeCreateDTO;
import com.programmer.escrow.dispute.dto.DisputeResolveDTO;
import com.programmer.escrow.dispute.entity.DisputeEntity;
import com.programmer.escrow.dispute.mapper.DisputeMapper;
import com.programmer.escrow.dispute.vo.DisputeVO;
import com.programmer.escrow.infra.sequence.BizNoGenerator;
import com.programmer.escrow.order.entity.OrderEntity;
import com.programmer.escrow.order.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class DisputeServiceImpl implements DisputeService {

    private final DisputeMapper disputeMapper;
    private final OrderMapper orderMapper;
    private final DemandMapper demandMapper;
    private final BizNoGenerator bizNoGenerator;

    public DisputeServiceImpl(DisputeMapper disputeMapper,
                              OrderMapper orderMapper,
                              DemandMapper demandMapper,
                              BizNoGenerator bizNoGenerator) {
        this.disputeMapper = disputeMapper;
        this.orderMapper = orderMapper;
        this.demandMapper = demandMapper;
        this.bizNoGenerator = bizNoGenerator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DisputeVO createDispute(Long initiatorId, DisputeCreateDTO dto) {
        OrderEntity order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BizException(404, "order does not exist");
        }
        if (!Objects.equals(order.getClientId(), initiatorId)
                && !Objects.equals(order.getDeveloperId(), initiatorId)) {
            throw new BizException(403, "you are not allowed to dispute this order");
        }
        if (Objects.equals(order.getStatus(), OrderStatusEnum.COMPLETED.getCode())
                || Objects.equals(order.getStatus(), OrderStatusEnum.CANCELLED.getCode())) {
            throw new BizException(400, "closed orders cannot enter dispute");
        }
        if (Objects.equals(order.getStatus(), OrderStatusEnum.DISPUTE.getCode())) {
            throw new BizException(409, "dispute is already in progress for this order");
        }

        int updated = orderMapper.updateToDispute(order.getId());
        if (updated == 0) {
            throw new BizException(409, "order status changed, please refresh and retry");
        }

        DisputeEntity entity = new DisputeEntity();
        entity.setDisputeNo(bizNoGenerator.nextDisputeNo());
        entity.setOrderId(dto.getOrderId());
        entity.setInitiatorId(initiatorId);
        entity.setRespondentId(Objects.equals(order.getClientId(), initiatorId)
                ? order.getDeveloperId()
                : order.getClientId());
        entity.setDisputeType(dto.getDisputeType());
        entity.setReason(dto.getReason());
        entity.setDetail(dto.getDetail());
        entity.setEvidenceJson(JsonArrayUtils.toJson(dto.getEvidences()));
        entity.setFreezeAmount(order.getEscrowAmount());
        entity.setStatus(0);
        entity.setResultType(0);
        entity.setVersion(0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        disputeMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DisputeVO resolveDispute(Long adminId, Long disputeId, DisputeResolveDTO dto) {
        DisputeEntity dispute = disputeMapper.selectById(disputeId);
        if (dispute == null) {
            throw new BizException(404, "dispute does not exist");
        }
        if (Objects.equals(dispute.getStatus(), 3) || Objects.equals(dispute.getStatus(), 5)) {
            throw new BizException(400, "dispute has already been resolved");
        }

        LocalDateTime now = LocalDateTime.now();
        int updated = disputeMapper.updateResolved(
                disputeId,
                adminId,
                dto.getResultType(),
                dto.getResolutionNote(),
                now,
                now
        );
        if (updated == 0) {
            throw new BizException(400, "dispute status does not allow resolution");
        }

        applyDisputeResolution(dispute.getOrderId(), dto.getResultType());
        return toVO(disputeMapper.selectById(disputeId));
    }

    @Override
    public List<DisputeVO> listMyDisputes(Long userId) {
        return disputeMapper.selectByUserId(userId).stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public List<DisputeVO> listAllDisputes() {
        return disputeMapper.selectAll().stream()
                .map(this::toVO)
                .toList();
    }

    private void applyDisputeResolution(Long orderId, Integer resultType) {
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(404, "order does not exist");
        }

        if (Objects.equals(resultType, 1)) {
            orderMapper.updateAfterDispute(orderId, OrderStatusEnum.CANCELLED.getCode(), 3);
            demandMapper.updateStatus(order.getDemandId(), 7);
            return;
        }
        if (Objects.equals(resultType, 2)) {
            orderMapper.updateAfterDispute(orderId, OrderStatusEnum.COMPLETED.getCode(), 2);
            demandMapper.updateStatus(order.getDemandId(), 6);
            return;
        }
        if (Objects.equals(resultType, 3)) {
            orderMapper.updateAfterDispute(orderId, OrderStatusEnum.COMPLETED.getCode(), 4);
            demandMapper.updateStatus(order.getDemandId(), 6);
            return;
        }
        if (Objects.equals(resultType, 4)) {
            orderMapper.updateAfterDispute(orderId, OrderStatusEnum.IN_PROGRESS.getCode(), 1);
            demandMapper.updateStatus(order.getDemandId(), 5);
            return;
        }
        throw new BizException(400, "invalid dispute resolution result");
    }

    private DisputeVO toVO(DisputeEntity entity) {
        return DisputeVO.builder()
                .id(entity.getId())
                .disputeNo(entity.getDisputeNo())
                .orderId(entity.getOrderId())
                .initiatorId(entity.getInitiatorId())
                .disputeType(entity.getDisputeType())
                .reason(entity.getReason())
                .detail(entity.getDetail())
                .status(entity.getStatus())
                .resultType(entity.getResultType())
                .freezeAmount(entity.getFreezeAmount())
                .evidences(JsonArrayUtils.toStringList(entity.getEvidenceJson()))
                .build();
    }
}
