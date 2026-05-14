package com.programmer.escrow.community.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommunityReplyVO {
    private Long id;
    private Long postId;
    private String authorName;
    private String content;
    private Integer likeCount;
    private LocalDateTime createdAt;
}
