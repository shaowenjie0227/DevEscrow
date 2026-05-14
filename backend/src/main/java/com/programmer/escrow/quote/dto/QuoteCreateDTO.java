package com.programmer.escrow.quote.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class QuoteCreateDTO {

    @NotNull(message = "需求ID不能为空")
    private Long demandId;

    @NotNull(message = "报价金额不能为空")
    @DecimalMin(value = "0.00", message = "报价金额不能小于0")
    private BigDecimal priceTotal;

    @NotNull(message = "预计工期不能为空")
    private Integer estimatedDays;

    @NotBlank(message = "技术方案不能为空")
    private String techSolution;

    private String deliveryDesc;

    private List<String> attachments;
}
