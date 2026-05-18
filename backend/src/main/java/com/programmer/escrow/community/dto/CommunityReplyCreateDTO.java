package com.programmer.escrow.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommunityReplyCreateDTO {
    private Long parentReplyId;

    @NotBlank(message = "回复内容不能为空")
    @Size(min = 2, max = 2000, message = "回复长度需在2到2000个字符之间")
    private String content;
}
