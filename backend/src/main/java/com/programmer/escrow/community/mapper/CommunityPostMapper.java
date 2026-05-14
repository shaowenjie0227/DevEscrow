package com.programmer.escrow.community.mapper;

import com.programmer.escrow.community.entity.CommunityPostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommunityPostMapper {
    int insert(CommunityPostEntity entity);
    CommunityPostEntity selectById(@Param("id") Long id);
    List<CommunityPostEntity> selectActiveList();
    int incrementLike(@Param("id") Long id);
    int incrementFavorite(@Param("id") Long id);
    int incrementReplyCount(@Param("id") Long id);
}
