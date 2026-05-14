package com.programmer.escrow.demand.dto;

import com.programmer.escrow.demand.model.DemandFileItem;
import com.programmer.escrow.demand.model.DemandStagePlan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DemandCreateDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "摘要不能为空")
    private String summary;

    @NotBlank(message = "详细描述不能为空")
    private String detail;

    @NotBlank(message = "分类不能为空")
    private String category;

    @NotNull(message = "预算下限不能为空")
    @DecimalMin(value = "0.00", message = "预算下限不能小于0")
    private BigDecimal budgetMin;

    @NotNull(message = "预算上限不能为空")
    @DecimalMin(value = "0.00", message = "预算上限不能小于0")
    private BigDecimal budgetMax;

    @NotNull(message = "预计工期不能为空")
    private Integer expectedDays;

    @NotNull(message = "交付类型不能为空")
    private Integer deliveryType;

    @Valid
    private List<DemandFileItem> images;

    @Valid
    private List<DemandFileItem> attachments;

    @Valid
    private List<DemandStagePlan> stagePlans;
}
