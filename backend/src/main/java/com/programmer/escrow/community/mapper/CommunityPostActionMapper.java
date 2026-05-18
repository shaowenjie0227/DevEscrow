package com.programmer.escrow.community.mapper;

import com.programmer.escrow.community.entity.CommunityPostActionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommunityPostActionMapper {
    int insert(CommunityPostActionEntity entity);

    CommunityPostActionEntity selectByPostIdAndUserIdAndType(@Param("postId") Long postId,
                                                             @Param("userId") Long userId,
                                                             @Param("actionType") Integer actionType);

    List<CommunityPostActionEntity> selectActiveByUserAndPostIds(@Param("userId") Long userId,
                                                                 @Param("actionType") Integer actionType,
                                                                 @Param("postIds") List<Long> postIds);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteByPostId(@Param("postId") Long postId);
}
