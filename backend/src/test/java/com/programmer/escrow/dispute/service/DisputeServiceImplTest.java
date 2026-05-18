package com.programmer.escrow.dispute.service;

import com.programmer.escrow.common.enums.OrderStatusEnum;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.demand.mapper.DemandMapper;
import com.programmer.escrow.dispute.dto.DisputeCreateDTO;
import com.programmer.escrow.dispute.dto.DisputeResolveDTO;
import com.programmer.escrow.dispute.entity.DisputeEntity;
import com.programmer.escrow.dispute.mapper.DisputeMapper;
import com.programmer.escrow.infra.sequence.BizNoGenerator;
import com.programmer.escrow.order.entity.OrderEntity;
import com.programmer.escrow.order.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisputeServiceImplTest {

    @Mock
    private DisputeMapper disputeMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private DemandMapper demandMapper;

    @Mock
    private BizNoGenerator bizNoGenerator;

    @InjectMocks
    private DisputeServiceImpl disputeService;

    @Test
    void createDisputeShouldLockOrderBeforeInsert() {
        OrderEntity order = buildOrder(OrderStatusEnum.IN_PROGRESS.getCode());
        DisputeCreateDTO dto = buildCreateDto();
        when(orderMapper.selectById(10L)).thenReturn(order);
        when(orderMapper.updateToDispute(10L)).thenReturn(1);
        when(bizNoGenerator.nextDisputeNo()).thenReturn("DP0001");

        disputeService.createDispute(1L, dto);

        InOrder inOrder = inOrder(orderMapper, disputeMapper);
        inOrder.verify(orderMapper).selectById(10L);
        inOrder.verify(orderMapper).updateToDispute(10L);
        inOrder.verify(disputeMapper).insert(any(DisputeEntity.class));
    }

    @Test
    void createDisputeShouldRejectWhenOrderStateChangesDuringLock() {
        OrderEntity order = buildOrder(OrderStatusEnum.IN_PROGRESS.getCode());
        DisputeCreateDTO dto = buildCreateDto();
        when(orderMapper.selectById(10L)).thenReturn(order);
        when(orderMapper.updateToDispute(10L)).thenReturn(0);

        BizException exception = assertThrows(BizException.class, () -> disputeService.createDispute(1L, dto));

        assertEquals(409, exception.getCode());
        verify(disputeMapper, never()).insert(any(DisputeEntity.class));
    }

    @Test
    void resolveDisputeShouldSyncDemandWhenRefundingClient() {
        DisputeEntity openDispute = new DisputeEntity();
        openDispute.setId(100L);
        openDispute.setOrderId(10L);
        openDispute.setStatus(0);

        DisputeEntity resolvedDispute = new DisputeEntity();
        resolvedDispute.setId(100L);
        resolvedDispute.setOrderId(10L);
        resolvedDispute.setStatus(3);
        resolvedDispute.setResultType(1);

        OrderEntity order = buildOrder(OrderStatusEnum.DISPUTE.getCode());
        when(disputeMapper.selectById(100L)).thenReturn(openDispute, resolvedDispute);
        when(disputeMapper.updateResolved(eq(100L), eq(88L), eq(1), eq("refund client"), any(), any())).thenReturn(1);
        when(orderMapper.selectById(10L)).thenReturn(order);

        DisputeResolveDTO dto = new DisputeResolveDTO();
        dto.setResultType(1);
        dto.setResolutionNote("refund client");

        disputeService.resolveDispute(88L, 100L, dto);

        verify(orderMapper).updateAfterDispute(10L, OrderStatusEnum.CANCELLED.getCode(), 3);
        verify(demandMapper).updateStatus(55L, 7);
    }

    private OrderEntity buildOrder(Integer status) {
        OrderEntity order = new OrderEntity();
        order.setId(10L);
        order.setDemandId(55L);
        order.setClientId(1L);
        order.setDeveloperId(2L);
        order.setStatus(status);
        order.setEscrowAmount(BigDecimal.valueOf(3000));
        return order;
    }

    private DisputeCreateDTO buildCreateDto() {
        DisputeCreateDTO dto = new DisputeCreateDTO();
        dto.setOrderId(10L);
        dto.setDisputeType(2);
        dto.setReason("quality issue");
        dto.setDetail("delivery does not match the agreed scope");
        return dto;
    }
}
