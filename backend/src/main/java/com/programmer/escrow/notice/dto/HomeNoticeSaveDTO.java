package com.programmer.escrow.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HomeNoticeSaveDTO {
    @NotNull(message = "内容类型不能为空")
    private Integer noticeType;
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "摘要不能为空")
    private String summary;
    private String targetUrl;
    private String coverUrl;
    @NotNull(message = "排序值不能为空")
    private Integer sortOrder;
}
