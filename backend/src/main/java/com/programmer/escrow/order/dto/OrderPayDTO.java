package com.programmer.escrow.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderPayDTO {

    @NotBlank(message = "支付方式不能为空")
    private String payChannel;
}
