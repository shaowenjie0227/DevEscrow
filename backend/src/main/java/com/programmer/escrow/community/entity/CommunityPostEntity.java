package com.programmer.escrow.community.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunityPostEntity {
    private Long id;
    private Long creatorId;
    private String authorName;
    private String forumName;
    private String title;
    private String summary;
    private String content;
    private Integer replyCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
