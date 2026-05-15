package com.programmer.escrow.notice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HomeNoticeStatusDTO {
    @NotNull(message = "状态不能为空")
    private Integer status;
}
