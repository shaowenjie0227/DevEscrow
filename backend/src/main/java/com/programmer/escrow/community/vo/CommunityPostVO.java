package com.programmer.escrow.community.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommunityPostVO {
    private Long id;
    private String authorName;
    private String forumName;
    private String title;
    private String summary;
    private String content;
    private Integer replyCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private LocalDateTime createdAt;
}
