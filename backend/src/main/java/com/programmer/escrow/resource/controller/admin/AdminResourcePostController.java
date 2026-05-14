package com.programmer.escrow.resource.controller.admin;

import com.programmer.escrow.admin.context.AdminContextHolder;
import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.resource.dto.ResourcePostSaveDTO;
import com.programmer.escrow.resource.dto.ResourcePostStatusDTO;
import com.programmer.escrow.resource.service.ResourcePostService;
import com.programmer.escrow.resource.vo.ResourcePostVO;
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
@RequestMapping("/api/admin/resources")
public class AdminResourcePostController {

    private final ResourcePostService resourcePostService;

    public AdminResourcePostController(ResourcePostService resourcePostService) {
        this.resourcePostService = resourcePostService;
    }

    @GetMapping
    public ApiResponse<List<ResourcePostVO>> list() {
        return ApiResponse.success(resourcePostService.listAdmin());
    }

    @PostMapping
    public ApiResponse<ResourcePostVO> create(@Valid @RequestBody ResourcePostSaveDTO dto) {
        return ApiResponse.success(resourcePostService.create(AdminContextHolder.getRequiredAdminId(), dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<ResourcePostVO> update(@PathVariable Long id, @Valid @RequestBody ResourcePostSaveDTO dto) {
        return ApiResponse.success(resourcePostService.update(id, dto));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<AdminOperationVO> updateStatus(@PathVariable Long id, @Valid @RequestBody ResourcePostStatusDTO dto) {
        return ApiResponse.success(resourcePostService.updateStatus(id, dto));
    }
}
