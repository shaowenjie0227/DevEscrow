package com.programmer.escrow.demand.controller.developer;

import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.demand.service.DemandService;
import com.programmer.escrow.demand.vo.DemandDetailVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/developer/demands")
public class DeveloperDemandController {

    private final DemandService demandService;

    public DeveloperDemandController(DemandService demandService) {
        this.demandService = demandService;
    }

    @GetMapping
    public ApiResponse<List<DemandDetailVO>> listMarketDemands() {
        return ApiResponse.success(demandService.listMarketDemands());
    }

    @GetMapping("/{demandId}")
    public ApiResponse<DemandDetailVO> getDemandDetail(@PathVariable Long demandId) {
        return ApiResponse.success(demandService.getDemandDetail(demandId));
    }
}
