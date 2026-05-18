package com.programmer.escrow.chat.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageVO {

    private Long id;
    private Integer bizType;
    private Long demandId;
    private Long orderId;
    private Long senderId;
    private String senderNickname;
    private String senderAvatarUrl;
    private Long receiverId;
    private String receiverNickname;
    private String receiverAvatarUrl;
    private Integer msgType;
    private String content;
    private String fileUrl;
    private Integer status;
    private Integer readStatus;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private Boolean self;
}
