package com.programmer.escrow.order.mapper;

import com.programmer.escrow.order.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    int insert(OrderEntity entity);

    OrderEntity selectById(@Param("id") Long id);

    OrderEntity selectByDemandId(@Param("demandId") Long demandId);

    List<OrderEntity> selectByClientId(@Param("clientId") Long clientId);

    List<OrderEntity> selectByDeveloperId(@Param("developerId") Long developerId);

    List<OrderEntity> selectAdminList(@Param("status") Integer status,
                                      @Param("keyword") String keyword);

    int updateToPaid(@Param("id") Long id,
                     @Param("clientId") Long clientId,
                     @Param("paidAt") LocalDateTime paidAt);

    int updateToInProgress(@Param("id") Long id,
                           @Param("developerId") Long developerId,
                           @Param("startedAt") LocalDateTime startedAt);

    int updateToWaitAccept(@Param("id") Long id,
                           @Param("developerId") Long developerId,
                           @Param("currentStageNo") Integer currentStageNo,
                           @Param("submittedAt") LocalDateTime submittedAt,
                           @Param("progressPercent") Integer progressPercent);

    int updateToCompleted(@Param("id") Long id,
                          @Param("clientId") Long clientId,
                          @Param("currentStageNo") Integer currentStageNo,
                          @Param("acceptedAt") LocalDateTime acceptedAt,
                          @Param("completedAt") LocalDateTime completedAt);

    int updateToRejected(@Param("id") Long id,
                         @Param("clientId") Long clientId,
                         @Param("currentStageNo") Integer currentStageNo,
                         @Param("progressPercent") Integer progressPercent);

    int advanceToNextStage(@Param("id") Long id,
                           @Param("clientId") Long clientId,
                           @Param("currentStageNo") Integer currentStageNo,
                           @Param("nextStageNo") Integer nextStageNo,
                           @Param("progressPercent") Integer progressPercent);

    int updateToDispute(@Param("id") Long id);

    int updateAfterDispute(@Param("id") Long id,
                           @Param("status") Integer status,
                           @Param("payStatus") Integer payStatus);
}
