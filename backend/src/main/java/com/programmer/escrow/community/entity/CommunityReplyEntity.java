package com.programmer.escrow.community.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunityReplyEntity {
    private Long id;
    private Long postId;
    private Long creatorId;
    private String authorName;
    private String content;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
