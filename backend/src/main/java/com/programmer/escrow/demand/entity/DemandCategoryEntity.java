package com.programmer.escrow.demand.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DemandCategoryEntity {

    private Long id;
    private String categoryName;
    private Integer sortOrder;
    private Integer status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
