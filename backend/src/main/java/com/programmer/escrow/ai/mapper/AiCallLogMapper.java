package com.programmer.escrow.ai.mapper;

import com.programmer.escrow.ai.entity.AiCallLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiCallLogMapper {

    int insert(AiCallLogEntity entity);

    List<AiCallLogEntity> selectRecent(@Param("sceneCode") String sceneCode,
                                       @Param("status") Integer status,
                                       @Param("limit") Integer limit);
}
