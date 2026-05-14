package com.programmer.escrow.order.service;

import com.programmer.escrow.common.enums.OrderStatusEnum;
import com.programmer.escrow.common.exception.BizException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@Service
public class OrderStateMachineService {

    public void assertCanTransfer(OrderStatusEnum currentStatus, OrderStatusEnum targetStatus) {
        Set<OrderStatusEnum> allowedTargets = getAllowedTargets(currentStatus);
        if (!allowedTargets.contains(targetStatus)) {
            throw new BizException(
                    4001,
                    "订单状态不允许从 "
                            + currentStatus.getDesc()
                            + " 流转到 "
                            + targetStatus.getDesc()
            );
        }
    }

    private Set<OrderStatusEnum> getAllowedTargets(OrderStatusEnum currentStatus) {
        switch (currentStatus) {
            case WAIT_QUOTE:
                return EnumSet.of(OrderStatusEnum.QUOTED, OrderStatusEnum.CANCELLED);
            case QUOTED:
                return EnumSet.of(OrderStatusEnum.PAID, OrderStatusEnum.CANCELLED);
            case PAID:
                return EnumSet.of(OrderStatusEnum.IN_PROGRESS, OrderStatusEnum.CANCELLED, OrderStatusEnum.DISPUTE);
            case IN_PROGRESS:
                return EnumSet.of(OrderStatusEnum.WAIT_ACCEPT, OrderStatusEnum.DISPUTE);
            case WAIT_ACCEPT:
                return EnumSet.of(OrderStatusEnum.COMPLETED, OrderStatusEnum.IN_PROGRESS, OrderStatusEnum.DISPUTE);
            case DISPUTE:
                return EnumSet.of(OrderStatusEnum.IN_PROGRESS, OrderStatusEnum.COMPLETED, OrderStatusEnum.CANCELLED);
            case COMPLETED:
            case CANCELLED:
            default:
                return Collections.emptySet();
        }
    }
}
