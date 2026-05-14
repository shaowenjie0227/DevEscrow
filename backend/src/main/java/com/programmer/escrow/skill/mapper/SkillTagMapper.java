package com.programmer.escrow.skill.mapper;

import com.programmer.escrow.skill.entity.SkillTagEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SkillTagMapper {
    int insert(SkillTagEntity entity);
    int update(SkillTagEntity entity);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    SkillTagEntity selectById(@Param("id") Long id);
    SkillTagEntity selectByName(@Param("tagName") String tagName);
    List<SkillTagEntity> selectAdminList();
    List<SkillTagEntity> selectActiveList();
}
