package com.programmer.escrow.notice.controller.admin;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.notice.dto.HomeNoticeSaveDTO;
import com.programmer.escrow.notice.dto.HomeNoticeStatusDTO;
import com.programmer.escrow.notice.service.HomeNoticeService;
import com.programmer.escrow.notice.vo.HomeNoticeVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/home-notices")
public class AdminHomeNoticeController {

    private final HomeNoticeService homeNoticeService;

    public AdminHomeNoticeController(HomeNoticeService homeNoticeService) {
        this.homeNoticeService = homeNoticeService;
    }

    @GetMapping
    public ApiResponse<List<HomeNoticeVO>> listAdmin() {
        return ApiResponse.success(homeNoticeService.listAdmin());
    }

    @PostMapping
    public ApiResponse<HomeNoticeVO> create(@Valid @RequestBody HomeNoticeSaveDTO dto) {
        return ApiResponse.success(homeNoticeService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<HomeNoticeVO> update(@PathVariable Long id, @Valid @RequestBody HomeNoticeSaveDTO dto) {
        return ApiResponse.success(homeNoticeService.update(id, dto));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<AdminOperationVO> updateStatus(@PathVariable Long id, @Valid @RequestBody HomeNoticeStatusDTO dto) {
        return ApiResponse.success(homeNoticeService.updateStatus(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<AdminOperationVO> delete(@PathVariable Long id) {
        return ApiResponse.success(homeNoticeService.delete(id));
    }
}
