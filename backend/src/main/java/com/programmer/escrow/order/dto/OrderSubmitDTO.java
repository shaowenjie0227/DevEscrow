package com.programmer.escrow.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class OrderSubmitDTO {

    @NotBlank(message = "提交说明不能为空")
    private String submitContent;

    private List<String> deliverables;
}
