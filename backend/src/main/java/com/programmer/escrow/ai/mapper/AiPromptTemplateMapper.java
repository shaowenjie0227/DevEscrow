package com.programmer.escrow.ai.mapper;

import com.programmer.escrow.ai.entity.AiPromptTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiPromptTemplateMapper {

    int insert(AiPromptTemplateEntity entity);

    int update(AiPromptTemplateEntity entity);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(@Param("id") Long id);

    AiPromptTemplateEntity selectById(@Param("id") Long id);

    AiPromptTemplateEntity selectBySceneCode(@Param("sceneCode") String sceneCode);

    AiPromptTemplateEntity selectActiveBySceneCode(@Param("sceneCode") String sceneCode);

    List<AiPromptTemplateEntity> selectAdminList();
}
