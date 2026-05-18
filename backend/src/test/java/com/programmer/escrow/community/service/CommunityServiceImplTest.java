package com.programmer.escrow.community.service;

import com.programmer.escrow.community.dto.CommunityPostCreateDTO;
import com.programmer.escrow.community.dto.CommunityReplyCreateDTO;
import com.programmer.escrow.community.dto.CommunityStatusDTO;
import com.programmer.escrow.community.entity.CommunityPostActionEntity;
import com.programmer.escrow.community.entity.CommunityPostEntity;
import com.programmer.escrow.community.entity.CommunityReplyEntity;
import com.programmer.escrow.community.mapper.CommunityPostActionMapper;
import com.programmer.escrow.community.mapper.CommunityPostMapper;
import com.programmer.escrow.community.mapper.CommunityReplyMapper;
import com.programmer.escrow.community.vo.CommunityPostVO;
import com.programmer.escrow.inbox.service.InboxMessageService;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityServiceImplTest {

    @Mock
    private CommunityPostMapper communityPostMapper;

    @Mock
    private CommunityReplyMapper communityReplyMapper;

    @Mock
    private CommunityPostActionMapper communityPostActionMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private InboxMessageService inboxMessageService;

    @InjectMocks
    private CommunityServiceImpl communityService;

    @Test
    void createPostShouldAutoGenerateSummaryWhenMissing() {
        UserEntity user = buildUser(2L, "Alice");
        CommunityPostCreateDTO dto = new CommunityPostCreateDTO();
        dto.setForumName("接单问答");
        dto.setTitle("如何梳理外包项目边界？");
        dto.setContent("我准备接一个小程序项目，想知道前期如何明确需求边界，避免后续反复返工。");

        when(userMapper.selectById(2L)).thenReturn(user);
        when(communityPostActionMapper.selectActiveByUserAndPostIds(eq(2L), eq(1), anyList())).thenReturn(Collections.emptyList());
        when(communityPostActionMapper.selectActiveByUserAndPostIds(eq(2L), eq(2), anyList())).thenReturn(Collections.emptyList());

        CommunityPostVO result = communityService.createPost(2L, dto);

        ArgumentCaptor<CommunityPostEntity> captor = ArgumentCaptor.forClass(CommunityPostEntity.class);
        verify(communityPostMapper).insert(captor.capture());
        assertEquals("Alice", captor.getValue().getAuthorName());
        assertTrue(captor.getValue().getSummary().startsWith("我准备接一个小程序项目"));
        assertEquals("如何梳理外包项目边界？", result.getTitle());
    }

    @Test
    void toggleLikePostShouldCreateActionAndReturnLikedState() {
        when(communityPostMapper.selectById(10L))
                .thenReturn(buildPost(10L, 1, 0, 0, 0), buildPost(10L, 1, 1, 0, 0));
        when(userMapper.selectById(2L)).thenReturn(buildUser(2L, "Alice"));
        when(communityPostActionMapper.selectByPostIdAndUserIdAndType(10L, 2L, 1)).thenReturn(null);
        when(communityPostActionMapper.selectActiveByUserAndPostIds(eq(2L), eq(1), anyList()))
                .thenReturn(List.of(buildAction(10L, 2L, 1, 1)));
        when(communityPostActionMapper.selectActiveByUserAndPostIds(eq(2L), eq(2), anyList()))
                .thenReturn(Collections.emptyList());

        CommunityPostVO result = communityService.toggleLikePost(2L, 10L);

        verify(communityPostActionMapper).insert(any(CommunityPostActionEntity.class));
        verify(communityPostMapper).incrementLike(10L);
        assertEquals(1, result.getLikeCount());
        assertTrue(Boolean.TRUE.equals(result.getLiked()));
    }

    @Test
    void updateReplyStatusShouldRefreshParentReplyCount() {
        CommunityReplyEntity reply = new CommunityReplyEntity();
        reply.setId(99L);
        reply.setPostId(10L);
        reply.setStatus(1);

        CommunityStatusDTO dto = new CommunityStatusDTO();
        dto.setStatus(2);

        when(communityReplyMapper.selectById(99L)).thenReturn(reply);
        when(communityReplyMapper.countActiveByPostId(10L)).thenReturn(3);

        communityService.updateReplyStatus(99L, dto);

        verify(communityReplyMapper).updateStatus(99L, 2);
        verify(communityPostMapper).updateReplyCount(10L, 3);
    }

    @Test
    void createReplyShouldBindParentReplyInfoWhenReplyingToComment() {
        CommunityReplyCreateDTO dto = new CommunityReplyCreateDTO();
        dto.setParentReplyId(8L);
        dto.setContent("这条建议很有用，我补充一个排期控制办法。");

        CommunityReplyEntity parentReply = new CommunityReplyEntity();
        parentReply.setId(8L);
        parentReply.setPostId(10L);
        parentReply.setCreatorId(6L);
        parentReply.setAuthorName("Bob");
        parentReply.setStatus(1);

        when(communityPostMapper.selectById(10L)).thenReturn(buildPost(10L, 9L, 1, 0, 0, 0));
        when(communityReplyMapper.selectById(8L)).thenReturn(parentReply);
        when(userMapper.selectById(2L)).thenReturn(buildUser(2L, "Alice"));
        when(communityReplyMapper.countActiveByPostId(10L)).thenReturn(1);

        communityService.createReply(2L, 10L, dto);

        ArgumentCaptor<CommunityReplyEntity> captor = ArgumentCaptor.forClass(CommunityReplyEntity.class);
        verify(communityReplyMapper).insert(captor.capture());
        assertEquals(8L, captor.getValue().getParentReplyId());
        assertEquals("Bob", captor.getValue().getReplyToAuthorName());
        verify(inboxMessageService).sendSystemMessage(eq(6L), eq("你的评论收到了新回复"), any(String.class), eq("/community/posts/10?replyId=8"));
        verify(inboxMessageService).sendSystemMessage(eq(9L), eq("你的帖子有了新的评论互动"), any(String.class), eq("/community/posts/10?replyId=8"));
    }

    @Test
    void createReplyShouldNotifyPostOwnerWhenReplyingToPost() {
        CommunityReplyCreateDTO dto = new CommunityReplyCreateDTO();
        dto.setContent("这个思路很清楚，我补充一下需求确认时要先锁定交付边界。");

        when(communityPostMapper.selectById(10L)).thenReturn(buildPost(10L, 8L, 1, 0, 0, 0));
        when(userMapper.selectById(2L)).thenReturn(buildUser(2L, "Alice"));
        when(communityReplyMapper.countActiveByPostId(10L)).thenReturn(1);
        doAnswer(invocation -> {
            CommunityReplyEntity entity = invocation.getArgument(0);
            entity.setId(18L);
            return 1;
        }).when(communityReplyMapper).insert(any(CommunityReplyEntity.class));

        communityService.createReply(2L, 10L, dto);

        verify(inboxMessageService).sendSystemMessage(eq(8L), eq("你的帖子收到了新回复"), any(String.class), eq("/community/posts/10?replyId=18"));
    }

    private UserEntity buildUser(Long id, String nickname) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setNickname(nickname);
        user.setUserNo("U" + id);
        return user;
    }

    private CommunityPostEntity buildPost(Long id, Integer status, Integer likeCount, Integer favoriteCount, Integer replyCount) {
        return buildPost(id, 2L, status, likeCount, favoriteCount, replyCount);
    }

    private CommunityPostEntity buildPost(Long id, Long creatorId, Integer status, Integer likeCount, Integer favoriteCount, Integer replyCount) {
        CommunityPostEntity entity = new CommunityPostEntity();
        entity.setId(id);
        entity.setCreatorId(creatorId);
        entity.setAuthorName("Alice");
        entity.setForumName("接单问答");
        entity.setTitle("如何梳理外包项目边界？");
        entity.setSummary("帖子摘要");
        entity.setContent("帖子正文");
        entity.setLikeCount(likeCount);
        entity.setFavoriteCount(favoriteCount);
        entity.setReplyCount(replyCount);
        entity.setStatus(status);
        return entity;
    }

    private CommunityPostActionEntity buildAction(Long postId, Long userId, Integer actionType, Integer status) {
        CommunityPostActionEntity entity = new CommunityPostActionEntity();
        entity.setPostId(postId);
        entity.setUserId(userId);
        entity.setActionType(actionType);
        entity.setStatus(status);
        return entity;
    }
}
