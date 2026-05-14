package com.programmer.escrow.quote.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QuoteEntity {

    private Long id;
    private String quoteNo;
    private Long demandId;
    private Long developerId;
    private BigDecimal priceTotal;
    private Integer estimatedDays;
    private String techSolution;
    private String deliveryDesc;
    private String attachJson;
    private LocalDateTime validUntil;
    private Integer status;
    private LocalDateTime selectedAt;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
