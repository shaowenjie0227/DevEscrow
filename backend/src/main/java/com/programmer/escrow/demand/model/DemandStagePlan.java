package com.programmer.escrow.demand.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DemandStagePlan {

    private Integer stageNo;

    private String stageName;

    @NotBlank(message = "阶段任务不能为空")
    private String stageDesc;

    @NotNull(message = "阶段结算金额不能为空")
    @DecimalMin(value = "0.01", message = "阶段结算金额必须大于0")
    private BigDecimal stageAmount;
}
