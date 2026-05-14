package com.programmer.escrow.kb.mapper;

import com.programmer.escrow.kb.entity.KnowledgeBaseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeBaseMapper {
    int insert(KnowledgeBaseEntity entity);
    int update(KnowledgeBaseEntity entity);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    KnowledgeBaseEntity selectById(@Param("id") Long id);
    List<KnowledgeBaseEntity> selectActiveList();
    List<KnowledgeBaseEntity> selectAdminList();
}
