package com.programmer.escrow.inbox.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InboxMessageVO {

    private Long id;
    private Long userId;
    private Long adminId;
    private String senderName;
    private String title;
    private String content;
    private String actionUrl;
    private Integer readStatus;
    private LocalDateTime readAt;
    private Integer status;
    private LocalDateTime createdAt;
}
