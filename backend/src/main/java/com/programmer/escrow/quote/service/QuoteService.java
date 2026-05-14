package com.programmer.escrow.quote.service;

import com.programmer.escrow.quote.dto.QuoteCreateDTO;
import com.programmer.escrow.quote.vo.QuoteVO;

import java.util.List;

public interface QuoteService {

    QuoteVO createQuote(Long developerId, QuoteCreateDTO dto);

    List<QuoteVO> listMyQuotes(Long developerId);

    List<QuoteVO> listDemandQuotes(Long clientId, Long demandId);
}
