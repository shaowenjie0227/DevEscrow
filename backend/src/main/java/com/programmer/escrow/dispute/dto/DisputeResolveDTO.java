package com.programmer.escrow.dispute.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DisputeResolveDTO {

    @NotNull(message = "resultType is required")
    @Min(value = 1, message = "resultType must be between 1 and 4")
    @Max(value = 4, message = "resultType must be between 1 and 4")
    private Integer resultType;

    @NotBlank(message = "resolutionNote is required")
    private String resolutionNote;
}
