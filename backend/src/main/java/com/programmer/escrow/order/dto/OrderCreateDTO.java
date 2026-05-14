package com.programmer.escrow.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateDTO {

    @NotNull(message = "需求ID不能为空")
    private Long demandId;

    @NotNull(message = "报价ID不能为空")
    private Long quoteId;
}
