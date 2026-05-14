package com.programmer.escrow.demand.controller.admin;

import com.programmer.escrow.admin.dto.DemandAuditDTO;
import com.programmer.escrow.admin.service.AdminOpsService;
import com.programmer.escrow.admin.service.AdminQueryService;
import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.demand.vo.DemandDetailVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/demands")
public class AdminDemandController {

    private final AdminOpsService adminOpsService;
    private final AdminQueryService adminQueryService;

    public AdminDemandController(AdminOpsService adminOpsService, AdminQueryService adminQueryService) {
        this.adminOpsService = adminOpsService;
        this.adminQueryService = adminQueryService;
    }

    @GetMapping
    public ApiResponse<List<DemandDetailVO>> listDemands(@RequestParam(required = false) Integer reviewStatus,
                                                         @RequestParam(required = false) Integer status) {
        return ApiResponse.success(adminQueryService.listDemands(reviewStatus, status));
    }

    @PostMapping("/{demandId}/approve")
    public ApiResponse<AdminOperationVO> approve(@PathVariable Long demandId, @Valid @RequestBody DemandAuditDTO dto) {
        dto.setReviewStatus(1);
        return ApiResponse.success(adminOpsService.auditDemand(demandId, dto));
    }

    @PostMapping("/{demandId}/reject")
    public ApiResponse<AdminOperationVO> reject(@PathVariable Long demandId, @Valid @RequestBody DemandAuditDTO dto) {
        dto.setReviewStatus(2);
        return ApiResponse.success(adminOpsService.auditDemand(demandId, dto));
    }
}
