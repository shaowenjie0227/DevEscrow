package com.programmer.escrow.quote.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class QuoteVO {

    private Long id;
    private String quoteNo;
    private Long demandId;
    private Long developerId;
    private BigDecimal priceTotal;
    private Integer estimatedDays;
    private String techSolution;
    private String deliveryDesc;
    private Integer status;
    private List<String> attachments;
}
