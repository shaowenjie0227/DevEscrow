package com.programmer.escrow.home.controller;

import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.home.service.HomeOverviewService;
import com.programmer.escrow.home.vo.HomeOverviewVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home/overview")
public class HomeOverviewController {

    private final HomeOverviewService homeOverviewService;

    public HomeOverviewController(HomeOverviewService homeOverviewService) {
        this.homeOverviewService = homeOverviewService;
    }

    @GetMapping
    public ApiResponse<HomeOverviewVO> overview() {
        return ApiResponse.success(homeOverviewService.getOverview());
    }
}
