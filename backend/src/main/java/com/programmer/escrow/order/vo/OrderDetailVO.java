package com.programmer.escrow.order.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OrderDetailVO {

    private Long id;
    private String orderNo;
    private Long demandId;
    private Long quoteId;
    private Long clientId;
    private Long developerId;
    private String orderTitle;
    private BigDecimal amountTotal;
    private BigDecimal escrowAmount;
    private Integer payStatus;
    private Integer status;
    private Integer currentStageNo;
    private Integer stageCount;
    private Integer progressPercent;
    private List<String> deliverables;
}
