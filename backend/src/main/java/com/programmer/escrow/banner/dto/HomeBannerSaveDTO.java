package com.programmer.escrow.banner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HomeBannerSaveDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "副标题不能为空")
    private String subtitle;
    private String buttonText;
    private String targetUrl;
    private String imageUrl;
    @NotNull(message = "排序值不能为空")
    private Integer sortOrder;
}
