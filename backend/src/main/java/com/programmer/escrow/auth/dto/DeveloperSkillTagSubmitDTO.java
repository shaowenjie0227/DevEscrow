package com.programmer.escrow.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class DeveloperSkillTagSubmitDTO {

    @NotEmpty(message = "技术栈不能为空")
    private List<Long> skillTagIds;
}
