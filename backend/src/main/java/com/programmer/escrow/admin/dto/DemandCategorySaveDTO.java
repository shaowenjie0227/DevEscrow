package com.programmer.escrow.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DemandCategorySaveDTO {

    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    @NotNull(message = "排序值不能为空")
    private Integer sortOrder;

    private String description;
}
