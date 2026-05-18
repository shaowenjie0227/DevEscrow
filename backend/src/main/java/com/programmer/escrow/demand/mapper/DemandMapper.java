package com.programmer.escrow.demand.mapper;

import com.programmer.escrow.demand.entity.DemandEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DemandMapper {

    int insert(DemandEntity entity);

    DemandEntity selectById(@Param("id") Long id);

    List<DemandEntity> selectByPublisherId(@Param("publisherId") Long publisherId);

    List<DemandEntity> selectMarketDemands();

    long countMarketDemands();

    List<DemandEntity> selectAdminList(@Param("reviewStatus") Integer reviewStatus,
                                       @Param("status") Integer status);

    int updateAuditStatus(@Param("id") Long id,
                          @Param("reviewStatus") Integer reviewStatus,
                          @Param("status") Integer status,
                          @Param("publishAt") LocalDateTime publishAt,
                          @Param("rejectReason") String rejectReason);

    int incrementQuoteCount(@Param("id") Long id, @Param("status") Integer status);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateCategorySnapshot(@Param("categoryId") Long categoryId, @Param("category") String category);

    int countByCategoryId(@Param("categoryId") Long categoryId);
}
