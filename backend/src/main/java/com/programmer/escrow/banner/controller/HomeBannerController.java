package com.programmer.escrow.banner.controller;

import com.programmer.escrow.banner.service.HomeBannerService;
import com.programmer.escrow.banner.vo.HomeBannerVO;
import com.programmer.escrow.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/home-banners")
public class HomeBannerController {

    private final HomeBannerService homeBannerService;

    public HomeBannerController(HomeBannerService homeBannerService) {
        this.homeBannerService = homeBannerService;
    }

    @GetMapping
    public ApiResponse<List<HomeBannerVO>> listActive() {
        return ApiResponse.success(homeBannerService.listActive());
    }
}
