package com.programmer.escrow.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderRemarkDTO {

    @NotBlank(message = "备注不能为空")
    private String remark;
}
