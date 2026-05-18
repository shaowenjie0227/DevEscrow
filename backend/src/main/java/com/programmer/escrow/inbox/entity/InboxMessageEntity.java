package com.programmer.escrow.inbox.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InboxMessageEntity {

    private Long id;
    private Long userId;
    private Long adminId;
    private String title;
    private String content;
    private String actionUrl;
    private Integer readStatus;
    private LocalDateTime readAt;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
