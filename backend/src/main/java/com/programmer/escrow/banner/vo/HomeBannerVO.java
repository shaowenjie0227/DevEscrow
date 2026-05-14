package com.programmer.escrow.banner.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HomeBannerVO {
    private Long id;
    private String title;
    private String subtitle;
    private String buttonText;
    private String targetUrl;
    private String imageUrl;
    private Integer sortOrder;
    private Integer status;
}
