package com.programmer.escrow.order.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderListVO {

    private Long id;
    private String orderNo;
    private String orderTitle;
    private BigDecimal amountTotal;
    private Integer status;
    private Integer payStatus;
    private Integer progressPercent;
}
