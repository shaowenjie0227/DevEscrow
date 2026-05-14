package com.programmer.escrow.banner.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HomeBannerStatusDTO {
    @NotNull(message = "状态不能为空")
    private Integer status;
}
