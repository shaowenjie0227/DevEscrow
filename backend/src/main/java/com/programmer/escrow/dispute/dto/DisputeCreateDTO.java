package com.programmer.escrow.dispute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class DisputeCreateDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "纠纷类型不能为空")
    private Integer disputeType;

    @NotBlank(message = "纠纷原因不能为空")
    private String reason;

    @NotBlank(message = "纠纷详情不能为空")
    private String detail;

    private List<String> evidences;
}
