package com.programmer.escrow.kb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KnowledgeBaseSaveDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "简介不能为空")
    private String intro;
    @NotBlank(message = "技术名称不能为空")
    private String techName;
    private String coverUrl;
    private String linkUrl;
    @NotNull(message = "排序值不能为空")
    private Integer sortOrder;
}
