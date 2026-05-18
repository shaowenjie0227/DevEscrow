package com.programmer.escrow.community.mapper;

import com.programmer.escrow.community.entity.CommunityPostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommunityPostMapper {
    int insert(CommunityPostEntity entity);

    CommunityPostEntity selectById(@Param("id") Long id);

    List<CommunityPostEntity> selectList(@Param("forumName") String forumName,
                                         @Param("keyword") String keyword,
                                         @Param("creatorId") Long creatorId,
                                         @Param("status") Integer status);

    int incrementLike(@Param("id") Long id);

    int decrementLike(@Param("id") Long id);

    int incrementFavorite(@Param("id") Long id);

    int decrementFavorite(@Param("id") Long id);

    int updateReplyCount(@Param("id") Long id, @Param("replyCount") Integer replyCount);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(@Param("id") Long id);
}
