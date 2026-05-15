package com.programmer.escrow.notice.controller;

import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.notice.service.HomeNoticeService;
import com.programmer.escrow.notice.vo.HomeNoticeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/home-notices")
public class HomeNoticeController {

    private final HomeNoticeService homeNoticeService;

    public HomeNoticeController(HomeNoticeService homeNoticeService) {
        this.homeNoticeService = homeNoticeService;
    }

    @GetMapping
    public ApiResponse<List<HomeNoticeVO>> listActive() {
        return ApiResponse.success(homeNoticeService.listActive());
    }
}
