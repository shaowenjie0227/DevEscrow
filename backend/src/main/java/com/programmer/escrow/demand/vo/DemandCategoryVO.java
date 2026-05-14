package com.programmer.escrow.demand.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DemandCategoryVO {

    private Long id;
    private String categoryName;
    private Integer sortOrder;
    private Integer status;
    private String description;
}
