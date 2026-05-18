package com.programmer.escrow.home.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeInsightVO {

    private String label;
    private String value;
    private String note;
}
