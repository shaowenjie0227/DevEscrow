package com.programmer.escrow.demand.controller.admin;

import com.programmer.escrow.admin.dto.DemandCategorySaveDTO;
import com.programmer.escrow.admin.dto.DemandCategoryStatusDTO;
import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.demand.service.DemandCategoryService;
import com.programmer.escrow.demand.vo.DemandCategoryVO;
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
@RequestMapping("/api/admin/demand-categories")
public class AdminDemandCategoryController {

    private final DemandCategoryService demandCategoryService;

    public AdminDemandCategoryController(DemandCategoryService demandCategoryService) {
        this.demandCategoryService = demandCategoryService;
    }

    @GetMapping
    public ApiResponse<List<DemandCategoryVO>> listCategories() {
        return ApiResponse.success(demandCategoryService.listAdminCategories());
    }

    @PostMapping
    public ApiResponse<DemandCategoryVO> createCategory(@Valid @RequestBody DemandCategorySaveDTO dto) {
        return ApiResponse.success(demandCategoryService.createCategory(dto));
    }

    @PutMapping("/{categoryId}")
    public ApiResponse<DemandCategoryVO> updateCategory(@PathVariable Long categoryId,
                                                        @Valid @RequestBody DemandCategorySaveDTO dto) {
        return ApiResponse.success(demandCategoryService.updateCategory(categoryId, dto));
    }

    @PostMapping("/{categoryId}/status")
    public ApiResponse<AdminOperationVO> updateCategoryStatus(@PathVariable Long categoryId,
                                                              @Valid @RequestBody DemandCategoryStatusDTO dto) {
        return ApiResponse.success(demandCategoryService.updateCategoryStatus(categoryId, dto));
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<AdminOperationVO> deleteCategory(@PathVariable Long categoryId) {
        return ApiResponse.success(demandCategoryService.deleteCategory(categoryId));
    }
}
