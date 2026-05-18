package com.programmer.escrow.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatSendDTO {

    @NotNull(message = "bizType is required")
    private Integer bizType;

    private Long demandId;

    private Long orderId;

    @NotNull(message = "receiverId is required")
    private Long receiverId;

    private Integer msgType;

    @NotBlank(message = "content is required")
    private String content;

    private String fileUrl;
}
