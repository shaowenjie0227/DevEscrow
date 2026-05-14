package com.programmer.escrow.skill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SkillTagSaveDTO {
    @NotBlank(message = "技能栈名称不能为空")
    private String tagName;

    @NotBlank(message = "技能栈类型不能为空")
    private String tagType;

    @NotNull(message = "排序值不能为空")
    private Integer sortOrder;
}
