package com.programmer.escrow.demand.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
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

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "summary is required")
    private String summary;

    @NotBlank(message = "detail is required")
    private String detail;

    @NotNull(message = "categoryId is required")
    private Long categoryId;

    private Integer orderType;

    @JsonAlias("isUrgent")
    private Boolean urgent;

    @DecimalMin(value = "0.00", message = "urgentBonus must be greater than or equal to 0")
    private BigDecimal urgentBonus;

    @NotNull(message = "budgetMin is required")
    @DecimalMin(value = "0.00", message = "budgetMin must be greater than or equal to 0")
    private BigDecimal budgetMin;

    @NotNull(message = "budgetMax is required")
    @DecimalMin(value = "0.00", message = "budgetMax must be greater than or equal to 0")
    private BigDecimal budgetMax;

    @NotNull(message = "expectedDays is required")
    private Integer expectedDays;

    @NotNull(message = "deliveryType is required")
    private Integer deliveryType;

    @Valid
    private List<DemandFileItem> images;

    @Valid
    private List<DemandFileItem> attachments;

    @Valid
    private List<DemandStagePlan> stagePlans;
}
