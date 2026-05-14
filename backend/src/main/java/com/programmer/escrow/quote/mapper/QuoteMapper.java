package com.programmer.escrow.quote.mapper;

import com.programmer.escrow.quote.entity.QuoteEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface QuoteMapper {

    int insert(QuoteEntity entity);

    QuoteEntity selectById(@Param("id") Long id);

    QuoteEntity selectByDemandAndDeveloper(@Param("demandId") Long demandId, @Param("developerId") Long developerId);

    List<QuoteEntity> selectByDeveloperId(@Param("developerId") Long developerId);

    List<QuoteEntity> selectByDemandId(@Param("demandId") Long demandId);

    int updateSelected(@Param("id") Long id, @Param("selectedAt") LocalDateTime selectedAt);

    int rejectOtherQuotes(@Param("demandId") Long demandId, @Param("selectedQuoteId") Long selectedQuoteId);
}
