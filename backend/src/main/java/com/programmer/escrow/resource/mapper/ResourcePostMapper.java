package com.programmer.escrow.resource.mapper;

import com.programmer.escrow.resource.entity.ResourcePostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResourcePostMapper {
    int insert(ResourcePostEntity entity);
    int update(ResourcePostEntity entity);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int deleteById(@Param("id") Long id);
    int incrementLike(@Param("id") Long id);
    int incrementFavorite(@Param("id") Long id);
    int incrementShare(@Param("id") Long id);
    ResourcePostEntity selectById(@Param("id") Long id);
    List<ResourcePostEntity> selectActiveList();
    List<ResourcePostEntity> selectAdminList();
}
