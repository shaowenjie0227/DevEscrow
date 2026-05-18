package com.programmer.escrow.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserMessageSendDTO {

    @NotBlank(message = "消息标题不能为空")
    @Size(max = 120, message = "消息标题不能超过120个字符")
    private String title;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容不能超过2000个字符")
    private String content;
}
