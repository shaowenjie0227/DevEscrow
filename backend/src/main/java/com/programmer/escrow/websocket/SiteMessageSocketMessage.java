package com.programmer.escrow.websocket;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SiteMessageSocketMessage {

    private String type;
    private Long messageId;
}
