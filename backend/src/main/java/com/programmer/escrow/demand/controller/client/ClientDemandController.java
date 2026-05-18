package com.programmer.escrow.demand.controller.client;

import com.programmer.escrow.auth.context.UserContextHolder;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.demand.dto.DemandCreateDTO;
import com.programmer.escrow.demand.service.DemandService;
import com.programmer.escrow.demand.vo.DemandDetailVO;
import com.programmer.escrow.quote.service.QuoteService;
import com.programmer.escrow.quote.vo.QuoteVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/client/demands")
public class ClientDemandController {

    private final DemandService demandService;
    private final QuoteService quoteService;

    public ClientDemandController(DemandService demandService, QuoteService quoteService) {
        this.demandService = demandService;
        this.quoteService = quoteService;
    }

    @PostMapping
    public ApiResponse<DemandDetailVO> createDemand(@Valid @RequestBody DemandCreateDTO dto) {
        return ApiResponse.success(demandService.createDemand(UserContextHolder.getRequiredUserId(), dto));
    }

    @GetMapping
    public ApiResponse<List<DemandDetailVO>> listMyDemands() {
        return ApiResponse.success(demandService.listMyDemands(UserContextHolder.getRequiredUserId()));
    }

    @GetMapping("/{demandId}")
    public ApiResponse<DemandDetailVO> getDemandDetail(@PathVariable Long demandId) {
        return ApiResponse.success(demandService.getMyDemandDetail(UserContextHolder.getRequiredUserId(), demandId));
    }

    @GetMapping("/{demandId}/quotes")
    public ApiResponse<List<QuoteVO>> listDemandQuotes(@PathVariable Long demandId) {
        return ApiResponse.success(quoteService.listDemandQuotes(UserContextHolder.getRequiredUserId(), demandId));
    }
}
