package com.programmer.escrow.community.mapper;

import com.programmer.escrow.community.entity.CommunityReplyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommunityReplyMapper {
    int insert(CommunityReplyEntity entity);

    CommunityReplyEntity selectById(@Param("id") Long id);

    List<CommunityReplyEntity> selectList(@Param("postId") Long postId, @Param("status") Integer status);

    int countActiveByPostId(@Param("postId") Long postId);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(@Param("id") Long id);

    int deleteByPostId(@Param("postId") Long postId);
}
