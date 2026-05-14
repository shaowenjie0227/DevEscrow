package com.programmer.escrow.skill.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SkillTagEntity {
    private Long id;
    private String tagName;
    private String tagType;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
