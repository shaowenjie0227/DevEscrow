package com.programmer.escrow.ai.mapper;

import com.programmer.escrow.ai.entity.AiRuntimeConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiRuntimeConfigMapper {

    AiRuntimeConfigEntity selectByConfigKey(@Param("configKey") String configKey);

    int insert(AiRuntimeConfigEntity entity);

    int update(AiRuntimeConfigEntity entity);
}
