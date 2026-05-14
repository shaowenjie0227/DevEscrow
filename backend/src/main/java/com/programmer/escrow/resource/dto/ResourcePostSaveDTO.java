package com.programmer.escrow.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResourcePostSaveDTO {
    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "介绍不能为空")
    private String intro;

    private String coverUrl;
    private String linkUrl;

    @NotNull(message = "排序值不能为空")
    private Integer sortOrder;
}
