package com.programmer.escrow.chat.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatConversationVO {

    private Integer bizType;
    private Long demandId;
    private Long orderId;
    private String demandNo;
    private String demandTitle;
    private Long partnerId;
    private String partnerNickname;
    private String partnerAvatarUrl;
    private Long clientId;
    private String clientNickname;
    private Long developerId;
    private String developerNickname;
    private String lastMessage;
    private Integer lastMessageType;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;
    private Integer messageCount;
}
