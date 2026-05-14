package com.programmer.escrow.dispute.controller.admin;

import com.programmer.escrow.admin.context.AdminContextHolder;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.dispute.dto.DisputeResolveDTO;
import com.programmer.escrow.dispute.service.DisputeService;
import com.programmer.escrow.dispute.vo.DisputeVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/disputes")
public class AdminDisputeController {

    private final DisputeService disputeService;

    public AdminDisputeController(DisputeService disputeService) {
        this.disputeService = disputeService;
    }

    @GetMapping
    public ApiResponse<List<DisputeVO>> listDisputes() {
        return ApiResponse.success(disputeService.listAllDisputes());
    }

    @PostMapping("/{disputeId}/resolve")
    public ApiResponse<DisputeVO> resolve(@PathVariable Long disputeId, @Valid @RequestBody DisputeResolveDTO dto) {
        return ApiResponse.success(disputeService.resolveDispute(AdminContextHolder.getRequiredAdminId(), disputeId, dto));
    }
}
