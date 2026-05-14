package com.programmer.escrow.quote.service;

import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.common.util.JsonArrayUtils;
import com.programmer.escrow.demand.entity.DemandEntity;
import com.programmer.escrow.demand.mapper.DemandMapper;
import com.programmer.escrow.infra.sequence.BizNoGenerator;
import com.programmer.escrow.quote.entity.QuoteEntity;
import com.programmer.escrow.quote.mapper.QuoteMapper;
import com.programmer.escrow.quote.dto.QuoteCreateDTO;
import com.programmer.escrow.quote.vo.QuoteVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class QuoteServiceImpl implements QuoteService {

    private final QuoteMapper quoteMapper;
    private final DemandMapper demandMapper;
    private final BizNoGenerator bizNoGenerator;

    public QuoteServiceImpl(QuoteMapper quoteMapper, DemandMapper demandMapper, BizNoGenerator bizNoGenerator) {
        this.quoteMapper = quoteMapper;
        this.demandMapper = demandMapper;
        this.bizNoGenerator = bizNoGenerator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuoteVO createQuote(Long developerId, QuoteCreateDTO dto) {
        DemandEntity demand = demandMapper.selectById(dto.getDemandId());
        if (demand == null) {
            throw new BizException(404, "需求不存在");
        }
        if (!Objects.equals(demand.getReviewStatus(), 1) || !(Objects.equals(demand.getStatus(), 2) || Objects.equals(demand.getStatus(), 3))) {
            throw new BizException(400, "该需求当前不可报价");
        }
        if (quoteMapper.selectByDemandAndDeveloper(dto.getDemandId(), developerId) != null) {
            throw new BizException(400, "你已经对该需求报过价");
        }

        QuoteEntity entity = new QuoteEntity();
        entity.setQuoteNo(bizNoGenerator.nextQuoteNo());
        entity.setDemandId(dto.getDemandId());
        entity.setDeveloperId(developerId);
        entity.setPriceTotal(dto.getPriceTotal());
        entity.setEstimatedDays(dto.getEstimatedDays());
        entity.setTechSolution(dto.getTechSolution());
        entity.setDeliveryDesc(dto.getDeliveryDesc());
        entity.setAttachJson(JsonArrayUtils.toJson(dto.getAttachments()));
        entity.setStatus(0);
        entity.setVersion(0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        quoteMapper.insert(entity);
        demandMapper.incrementQuoteCount(demand.getId(), 3);
        return toVO(entity);
    }

    @Override
    public List<QuoteVO> listMyQuotes(Long developerId) {
        return quoteMapper.selectByDeveloperId(developerId).stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public List<QuoteVO> listDemandQuotes(Long clientId, Long demandId) {
        DemandEntity demand = demandMapper.selectById(demandId);
        if (demand == null) {
            throw new BizException(404, "需求不存在");
        }
        if (!Objects.equals(demand.getPublisherId(), clientId)) {
            throw new BizException(403, "你无权查看该需求的报价");
        }
        return quoteMapper.selectByDemandId(demandId).stream()
                .map(this::toVO)
                .toList();
    }

    private QuoteVO toVO(QuoteEntity entity) {
        return QuoteVO.builder()
                .id(entity.getId())
                .quoteNo(entity.getQuoteNo())
                .demandId(entity.getDemandId())
                .developerId(entity.getDeveloperId())
                .priceTotal(entity.getPriceTotal())
                .estimatedDays(entity.getEstimatedDays())
                .techSolution(entity.getTechSolution())
                .deliveryDesc(entity.getDeliveryDesc())
                .status(entity.getStatus())
                .attachments(JsonArrayUtils.toStringList(entity.getAttachJson()))
                .build();
    }
}
