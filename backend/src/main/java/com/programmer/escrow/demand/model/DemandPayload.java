package com.programmer.escrow.demand.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class DemandPayload {

    private Integer orderType = 1;

    private Boolean urgent = false;

    private BigDecimal urgentBonus = BigDecimal.ZERO;

    private List<DemandFileItem> images = new ArrayList<>();

    private List<DemandFileItem> attachments = new ArrayList<>();

    private List<DemandStagePlan> stagePlans = new ArrayList<>();
}
