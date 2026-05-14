package com.programmer.escrow.kb.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KnowledgeBaseVO {
    private Long id;
    private String title;
    private String intro;
    private String techName;
    private String coverUrl;
    private String linkUrl;
    private Integer sortOrder;
    private Integer status;
}
