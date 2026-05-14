package com.programmer.escrow.community.service;

import com.programmer.escrow.community.dto.CommunityPostCreateDTO;
import com.programmer.escrow.community.dto.CommunityReplyCreateDTO;
import com.programmer.escrow.community.vo.CommunityPostVO;
import com.programmer.escrow.community.vo.CommunityReplyVO;

import java.util.List;

public interface CommunityService {
    List<CommunityPostVO> listPosts(String sortType);
    CommunityPostVO createPost(Long userId, CommunityPostCreateDTO dto);
    void likePost(Long postId);
    void favoritePost(Long postId);
    List<CommunityReplyVO> listReplies(Long postId);
    CommunityReplyVO createReply(Long userId, Long postId, CommunityReplyCreateDTO dto);
}
