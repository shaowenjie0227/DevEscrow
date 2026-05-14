package com.programmer.escrow.kb.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeBaseEntity {
    private Long id;
    private String title;
    private String intro;
    private String techName;
    private String coverUrl;
    private String linkUrl;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
