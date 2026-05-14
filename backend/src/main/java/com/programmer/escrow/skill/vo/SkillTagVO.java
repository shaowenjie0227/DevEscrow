package com.programmer.escrow.skill.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SkillTagVO {
    private Long id;
    private String tagName;
    private String tagType;
    private Integer sortOrder;
    private Integer status;
}
