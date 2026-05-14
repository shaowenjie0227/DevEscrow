package com.programmer.escrow.dispute.mapper;

import com.programmer.escrow.dispute.entity.DisputeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DisputeMapper {

    int insert(DisputeEntity entity);

    DisputeEntity selectById(@Param("id") Long id);

    List<DisputeEntity> selectByUserId(@Param("userId") Long userId);

    List<DisputeEntity> selectAll();

    int updateResolved(@Param("id") Long id,
                       @Param("adminId") Long adminId,
                       @Param("resultType") Integer resultType,
                       @Param("resolutionNote") String resolutionNote,
                       @Param("resolvedAt") LocalDateTime resolvedAt,
                       @Param("closedAt") LocalDateTime closedAt);
}
