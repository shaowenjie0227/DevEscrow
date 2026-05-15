package com.programmer.escrow.notice.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HomeNoticeEntity {
    private Long id;
    private Integer noticeType;
    private String title;
    private String summary;
    private String targetUrl;
    private String coverUrl;
    private Integer isPinned;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
