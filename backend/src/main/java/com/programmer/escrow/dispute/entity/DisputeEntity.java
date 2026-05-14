package com.programmer.escrow.dispute.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DisputeEntity {

    private Long id;
    private String disputeNo;
    private Long orderId;
    private Long initiatorId;
    private Long respondentId;
    private Long adminId;
    private Integer disputeType;
    private String reason;
    private String detail;
    private String evidenceJson;
    private BigDecimal freezeAmount;
    private Integer status;
    private Integer resultType;
    private String resolutionNote;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
