package com.programmer.escrow.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommunityPostCreateDTO {
    @NotBlank(message = "版块不能为空")
    @Size(max = 32, message = "版块名称不能超过32个字符")
    private String forumName;

    @NotBlank(message = "标题不能为空")
    @Size(min = 4, max = 128, message = "标题长度需在4到128个字符之间")
    private String title;

    @Size(max = 255, message = "摘要不能超过255个字符")
    private String summary;

    @NotBlank(message = "正文不能为空")
    @Size(max = 40000, message = "正文长度不能超过40000个字符")
    private String content;
}
