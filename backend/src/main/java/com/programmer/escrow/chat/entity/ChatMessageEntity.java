package com.programmer.escrow.chat.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageEntity {

    private Long id;
    private Integer bizType;
    private Long demandId;
    private Long orderId;
    private Long senderId;
    private Long receiverId;
    private Integer msgType;
    private String content;
    private String fileUrl;
    private String extraJson;
    private Integer readStatus;
    private Integer status;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
