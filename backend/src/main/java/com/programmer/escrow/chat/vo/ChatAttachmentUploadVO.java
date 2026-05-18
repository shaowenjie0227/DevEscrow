package com.programmer.escrow.chat.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatAttachmentUploadVO {

    private String url;
    private String fileName;
    private String originalName;
    private String contentType;
    private Long size;
    private LocalDateTime expiresAt;
}
