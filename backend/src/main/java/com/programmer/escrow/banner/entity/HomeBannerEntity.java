package com.programmer.escrow.banner.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HomeBannerEntity {
    private Long id;
    private String title;
    private String subtitle;
    private String buttonText;
    private String targetUrl;
    private String imageUrl;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
