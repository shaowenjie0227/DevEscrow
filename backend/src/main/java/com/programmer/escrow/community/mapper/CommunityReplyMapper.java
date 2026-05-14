package com.programmer.escrow.community.mapper;

import com.programmer.escrow.community.entity.CommunityReplyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommunityReplyMapper {
    int insert(CommunityReplyEntity entity);
    List<CommunityReplyEntity> selectByPostId(@Param("postId") Long postId);
}
