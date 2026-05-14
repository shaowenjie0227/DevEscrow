package com.programmer.escrow.demand.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DemandEntity {

    private Long id;
    private String demandNo;
    private Long publisherId;
    private String title;
    private String summary;
    private String detail;
    private Long categoryId;
    private String category;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private Integer expectedDays;
    private Integer deliveryType;
    private String attachmentsJson;
    private String contactNote;
    private Integer quoteCount;
    private Integer reviewStatus;
    private Integer status;
    private LocalDateTime publishAt;
    private LocalDateTime closedAt;
    private String rejectReason;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
