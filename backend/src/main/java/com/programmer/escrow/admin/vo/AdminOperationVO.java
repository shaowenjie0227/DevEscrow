package com.programmer.escrow.admin.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminOperationVO {

    private Long targetId;
    private Integer status;
    private String message;
}
