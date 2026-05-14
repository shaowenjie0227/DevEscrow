package com.programmer.escrow.resource.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourcePostEntity {
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
    private Long creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
