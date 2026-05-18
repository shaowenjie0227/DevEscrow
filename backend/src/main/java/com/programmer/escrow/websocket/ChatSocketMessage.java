package com.programmer.escrow.websocket;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatSocketMessage {

    private String type;
    private Integer bizType;
    private Long demandId;
    private Long orderId;
    private Long senderId;
    private Long receiverId;
}
