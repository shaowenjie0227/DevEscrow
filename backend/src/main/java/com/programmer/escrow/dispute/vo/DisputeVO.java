package com.programmer.escrow.dispute.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DisputeVO {

    private Long id;
    private String disputeNo;
    private Long orderId;
    private Long initiatorId;
    private Integer disputeType;
    private String reason;
    private String detail;
    private Integer status;
    private Integer resultType;
    private BigDecimal freezeAmount;
    private List<String> evidences;
}
