package com.programmer.escrow.resource.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourcePostVO {
    private Long id;
    private String title;
    private String intro;
    private String coverUrl;
    private String linkUrl;
    private Integer sortOrder;
    private Integer status;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer shareCount;
}
