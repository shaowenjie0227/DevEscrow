package com.programmer.escrow.dispute.controller.developer;

import com.programmer.escrow.auth.context.UserContextHolder;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.dispute.dto.DisputeCreateDTO;
import com.programmer.escrow.dispute.service.DisputeService;
import com.programmer.escrow.dispute.vo.DisputeVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/developer/disputes")
public class DeveloperDisputeController {

    private final DisputeService disputeService;

    public DeveloperDisputeController(DisputeService disputeService) {
        this.disputeService = disputeService;
    }

    @PostMapping
    public ApiResponse<DisputeVO> createDispute(@Valid @RequestBody DisputeCreateDTO dto) {
        return ApiResponse.success(disputeService.createDispute(UserContextHolder.getRequiredUserId(), dto));
    }

    @GetMapping
    public ApiResponse<List<DisputeVO>> listDisputes() {
        return ApiResponse.success(disputeService.listMyDisputes(UserContextHolder.getRequiredUserId()));
    }
}
