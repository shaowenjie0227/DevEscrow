package com.programmer.escrow.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderRejectDTO {

    @NotBlank(message = "驳回原因不能为空")
    private String reason;
}
