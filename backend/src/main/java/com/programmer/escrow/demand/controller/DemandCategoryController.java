package com.programmer.escrow.demand.controller;

import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.demand.service.DemandCategoryService;
import com.programmer.escrow.demand.vo.DemandCategoryVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/demand-categories")
public class DemandCategoryController {

    private final DemandCategoryService demandCategoryService;

    public DemandCategoryController(DemandCategoryService demandCategoryService) {
        this.demandCategoryService = demandCategoryService;
    }

    @GetMapping
    public ApiResponse<List<DemandCategoryVO>> listActiveCategories() {
        return ApiResponse.success(demandCategoryService.listActiveCategories());
    }
}
