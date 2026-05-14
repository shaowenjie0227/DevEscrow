package com.programmer.escrow.order.mapper;

import com.programmer.escrow.order.entity.OrderStageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface OrderStageMapper {

    int insert(OrderStageEntity entity);

    OrderStageEntity selectByOrderIdAndStageNo(@Param("orderId") Long orderId, @Param("stageNo") Integer stageNo);

    int countByOrderId(@Param("orderId") Long orderId);

    int updateToInProgress(@Param("orderId") Long orderId,
                           @Param("stageNo") Integer stageNo,
                           @Param("actualStartAt") LocalDateTime actualStartAt);

    int updateToSubmitted(@Param("orderId") Long orderId,
                          @Param("stageNo") Integer stageNo,
                          @Param("submitContent") String submitContent,
                          @Param("deliverableJson") String deliverableJson,
                          @Param("actualSubmitAt") LocalDateTime actualSubmitAt);

    int updateToAccepted(@Param("orderId") Long orderId,
                         @Param("stageNo") Integer stageNo,
                         @Param("actualAcceptAt") LocalDateTime actualAcceptAt);

    int updateToRejected(@Param("orderId") Long orderId,
                         @Param("stageNo") Integer stageNo,
                         @Param("rejectReason") String rejectReason);
}
