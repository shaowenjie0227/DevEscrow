package com.programmer.escrow.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommunityPostCreateDTO {
    @NotBlank(message = "版块不能为空")
    private String forumName;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "摘要不能为空")
    private String summary;

    @NotBlank(message = "正文不能为空")
    private String content;
}
