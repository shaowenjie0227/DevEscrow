package com.programmer.escrow.order.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderStageEntity {

    private Long id;
    private Long orderId;
    private Integer stageNo;
    private String stageName;
    private String stageDesc;
    private BigDecimal stageAmount;
    private LocalDateTime planStartAt;
    private LocalDateTime planEndAt;
    private String submitContent;
    private String deliverableJson;
    private Integer status;
    private LocalDateTime actualStartAt;
    private LocalDateTime actualSubmitAt;
    private LocalDateTime actualAcceptAt;
    private String rejectReason;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
