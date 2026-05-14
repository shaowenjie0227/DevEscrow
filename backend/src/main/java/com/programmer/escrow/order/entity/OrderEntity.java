package com.programmer.escrow.order.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderEntity {

    private Long id;
    private String orderNo;
    private Long demandId;
    private Long quoteId;
    private Long clientId;
    private Long developerId;
    private String orderTitle;
    private BigDecimal amountTotal;
    private BigDecimal escrowAmount;
    private BigDecimal platformFee;
    private Integer payStatus;
    private Integer status;
    private Integer currentStageNo;
    private Integer progressPercent;
    private LocalDateTime selectedAt;
    private LocalDateTime paidAt;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
