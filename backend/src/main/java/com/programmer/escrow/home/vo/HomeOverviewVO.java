package com.programmer.escrow.home.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeOverviewVO {

    private HomeAdminContactVO adminContact;
    private List<HomeHeroMetricVO> heroMetrics;
    private List<HomeInsightVO> stats;
    private List<HomeInsightVO> marketSignals;
    private List<String> tradingRules;
}
