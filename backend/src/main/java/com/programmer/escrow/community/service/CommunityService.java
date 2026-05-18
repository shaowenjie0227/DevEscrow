package com.programmer.escrow.community.service;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.community.dto.CommunityPostCreateDTO;
import com.programmer.escrow.community.dto.CommunityReplyCreateDTO;
import com.programmer.escrow.community.dto.CommunityStatusDTO;
import com.programmer.escrow.community.vo.CommunityPostVO;
import com.programmer.escrow.community.vo.CommunityReplyVO;

import java.util.List;

public interface CommunityService {
    List<CommunityPostVO> listPosts(String sortType, String forumName, String keyword, Boolean mine, Long currentUserId);

    CommunityPostVO getPost(Long postId, Long currentUserId);

    CommunityPostVO createPost(Long userId, CommunityPostCreateDTO dto);

    CommunityPostVO toggleLikePost(Long userId, Long postId);

    CommunityPostVO toggleFavoritePost(Long userId, Long postId);

    List<CommunityReplyVO> listReplies(Long postId);

    CommunityReplyVO createReply(Long userId, Long postId, CommunityReplyCreateDTO dto);

    List<CommunityPostVO> listAdminPosts(String forumName, String keyword, Integer status);

    List<CommunityReplyVO> listAdminReplies(Long postId, Integer status);

    AdminOperationVO updatePostStatus(Long postId, CommunityStatusDTO dto);

    AdminOperationVO deletePost(Long postId);

    AdminOperationVO updateReplyStatus(Long replyId, CommunityStatusDTO dto);

    AdminOperationVO deleteReply(Long replyId);
}
