package com.programmer.escrow.inbox.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InboxUnreadSummaryVO {

    private Integer unreadCount;
    private String latestTitle;
    private LocalDateTime latestCreatedAt;
}
