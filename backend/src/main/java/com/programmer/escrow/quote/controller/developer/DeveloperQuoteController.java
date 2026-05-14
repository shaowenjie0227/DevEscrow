package com.programmer.escrow.quote.controller.developer;

import com.programmer.escrow.auth.context.UserContextHolder;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.quote.dto.QuoteCreateDTO;
import com.programmer.escrow.quote.service.QuoteService;
import com.programmer.escrow.quote.vo.QuoteVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/developer/quotes")
public class DeveloperQuoteController {

    private final QuoteService quoteService;

    public DeveloperQuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping
    public ApiResponse<QuoteVO> createQuote(@Valid @RequestBody QuoteCreateDTO dto) {
        return ApiResponse.success(quoteService.createQuote(UserContextHolder.getRequiredUserId(), dto));
    }

    @GetMapping("/my")
    public ApiResponse<List<QuoteVO>> listMyQuotes() {
        return ApiResponse.success(quoteService.listMyQuotes(UserContextHolder.getRequiredUserId()));
    }
}
