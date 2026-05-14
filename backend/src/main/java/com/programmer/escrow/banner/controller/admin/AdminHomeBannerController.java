package com.programmer.escrow.banner.controller.admin;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.banner.dto.HomeBannerSaveDTO;
import com.programmer.escrow.banner.dto.HomeBannerStatusDTO;
import com.programmer.escrow.banner.service.HomeBannerService;
import com.programmer.escrow.banner.vo.HomeBannerVO;
import com.programmer.escrow.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/admin/home-banners", "/api/admin/banners"})
public class AdminHomeBannerController {

    private final HomeBannerService homeBannerService;

    public AdminHomeBannerController(HomeBannerService homeBannerService) {
        this.homeBannerService = homeBannerService;
    }

    @GetMapping
    public ApiResponse<List<HomeBannerVO>> listAdmin() {
        return ApiResponse.success(homeBannerService.listAdmin());
    }

    @PostMapping
    public ApiResponse<HomeBannerVO> create(@Valid @RequestBody HomeBannerSaveDTO dto) {
        return ApiResponse.success(homeBannerService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<HomeBannerVO> update(@PathVariable Long id, @Valid @RequestBody HomeBannerSaveDTO dto) {
        return ApiResponse.success(homeBannerService.update(id, dto));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<AdminOperationVO> updateStatus(@PathVariable Long id, @Valid @RequestBody HomeBannerStatusDTO dto) {
        return ApiResponse.success(homeBannerService.updateStatus(id, dto));
    }
}
