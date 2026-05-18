package com.programmer.escrow.community.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunityPostActionEntity {
    private Long id;
    private Long postId;
    private Long userId;
    private Integer actionType;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
