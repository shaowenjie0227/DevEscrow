package com.programmer.escrow.demand.vo;

import com.programmer.escrow.demand.model.DemandFileItem;
import com.programmer.escrow.demand.model.DemandStagePlan;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DemandDetailVO {

    private Long id;
    private String demandNo;
    private String title;
    private String summary;
    private String detail;
    private Long categoryId;
    private String category;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private Integer expectedDays;
    private Integer deliveryType;
    private Integer quoteCount;
    private Integer reviewStatus;
    private Integer status;
    private String coverImage;
    private List<DemandFileItem> images;
    private List<DemandFileItem> attachments;
    private List<DemandStagePlan> stagePlans;
}
