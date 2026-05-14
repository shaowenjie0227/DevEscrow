package com.programmer.escrow.community.service;

import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.community.dto.CommunityPostCreateDTO;
import com.programmer.escrow.community.dto.CommunityReplyCreateDTO;
import com.programmer.escrow.community.entity.CommunityPostEntity;
import com.programmer.escrow.community.entity.CommunityReplyEntity;
import com.programmer.escrow.community.mapper.CommunityPostMapper;
import com.programmer.escrow.community.mapper.CommunityReplyMapper;
import com.programmer.escrow.community.vo.CommunityPostVO;
import com.programmer.escrow.community.vo.CommunityReplyVO;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class CommunityServiceImpl implements CommunityService {

    private final CommunityPostMapper communityPostMapper;
    private final CommunityReplyMapper communityReplyMapper;
    private final UserMapper userMapper;

    public CommunityServiceImpl(CommunityPostMapper communityPostMapper,
                                CommunityReplyMapper communityReplyMapper,
                                UserMapper userMapper) {
        this.communityPostMapper = communityPostMapper;
        this.communityReplyMapper = communityReplyMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<CommunityPostVO> listPosts(String sortType) {
        List<CommunityPostVO> list = communityPostMapper.selectActiveList().stream().map(this::toPostVO).toList();
        if ("hot".equalsIgnoreCase(sortType)) {
            return list.stream()
                    .sorted(Comparator.comparingInt((CommunityPostVO item) -> safe(item.getLikeCount()) + safe(item.getReplyCount())).reversed())
                    .toList();
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityPostVO createPost(Long userId, CommunityPostCreateDTO dto) {
        UserEntity user = getUserOrThrow(userId);
        CommunityPostEntity entity = new CommunityPostEntity();
        entity.setCreatorId(userId);
        entity.setAuthorName(resolveAuthorName(user));
        entity.setForumName(dto.getForumName().trim());
        entity.setTitle(dto.getTitle().trim());
        entity.setSummary(dto.getSummary().trim());
        entity.setContent(dto.getContent().trim());
        entity.setReplyCount(0);
        entity.setLikeCount(0);
        entity.setFavoriteCount(0);
        entity.setStatus(1);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        communityPostMapper.insert(entity);
        return toPostVO(entity);
    }

    @Override
    public void likePost(Long postId) {
        getPostOrThrow(postId);
        communityPostMapper.incrementLike(postId);
    }

    @Override
    public void favoritePost(Long postId) {
        getPostOrThrow(postId);
        communityPostMapper.incrementFavorite(postId);
    }

    @Override
    public List<CommunityReplyVO> listReplies(Long postId) {
        getPostOrThrow(postId);
        return communityReplyMapper.selectByPostId(postId).stream().map(this::toReplyVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityReplyVO createReply(Long userId, Long postId, CommunityReplyCreateDTO dto) {
        getPostOrThrow(postId);
        UserEntity user = getUserOrThrow(userId);
        CommunityReplyEntity entity = new CommunityReplyEntity();
        entity.setPostId(postId);
        entity.setCreatorId(userId);
        entity.setAuthorName(resolveAuthorName(user));
        entity.setContent(dto.getContent().trim());
        entity.setLikeCount(0);
        entity.setStatus(1);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        communityReplyMapper.insert(entity);
        communityPostMapper.incrementReplyCount(postId);
        return toReplyVO(entity);
    }

    private CommunityPostEntity getPostOrThrow(Long postId) {
        CommunityPostEntity entity = communityPostMapper.selectById(postId);
        if (entity == null || entity.getStatus() == null || entity.getStatus() != 1) {
            throw new BizException(404, "帖子不存在");
        }
        return entity;
    }

    private UserEntity getUserOrThrow(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    private String resolveAuthorName(UserEntity user) {
        if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
            return user.getNickname().trim();
        }
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            return user.getPhone().trim();
        }
        return user.getUserNo();
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }

    private CommunityPostVO toPostVO(CommunityPostEntity entity) {
        return CommunityPostVO.builder()
                .id(entity.getId())
                .authorName(entity.getAuthorName())
                .forumName(entity.getForumName())
                .title(entity.getTitle())
                .summary(entity.getSummary())
                .content(entity.getContent())
                .replyCount(entity.getReplyCount())
                .likeCount(entity.getLikeCount())
                .favoriteCount(entity.getFavoriteCount())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private CommunityReplyVO toReplyVO(CommunityReplyEntity entity) {
        return CommunityReplyVO.builder()
                .id(entity.getId())
                .postId(entity.getPostId())
                .authorName(entity.getAuthorName())
                .content(entity.getContent())
                .likeCount(entity.getLikeCount())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
