package com.programmer.escrow.chat.service;

import com.programmer.escrow.chat.dto.ChatSendDTO;
import com.programmer.escrow.chat.entity.ChatMessageEntity;
import com.programmer.escrow.chat.mapper.ChatMessageMapper;
import com.programmer.escrow.chat.vo.ChatMessageVO;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.demand.entity.DemandEntity;
import com.programmer.escrow.demand.mapper.DemandMapper;
import com.programmer.escrow.order.mapper.OrderMapper;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import com.programmer.escrow.user.service.UserBanLifecycleService;
import com.programmer.escrow.websocket.ChatWebSocketSessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @Mock
    private DemandMapper demandMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ChatWebSocketSessionManager chatWebSocketSessionManager;

    @Mock
    private UserBanLifecycleService userBanLifecycleService;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    void sendMessageShouldAllowApprovedDeveloperToContactDemandPublisher() {
        DemandEntity demand = new DemandEntity();
        demand.setId(10L);
        demand.setPublisherId(1L);

        ChatSendDTO dto = new ChatSendDTO();
        dto.setBizType(1);
        dto.setDemandId(10L);
        dto.setReceiverId(1L);
        dto.setContent("Can we clarify the delivery scope?");

        when(userBanLifecycleService.normalizeBanStatus(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(demandMapper.selectById(10L)).thenReturn(demand);
        when(userMapper.selectById(1L)).thenReturn(buildUser(1L, "Client", 1, 0));
        when(userMapper.selectById(2L)).thenReturn(buildUser(2L, "Developer", 1, 2));

        ChatMessageVO result = chatService.sendMessage(2L, dto);

        ArgumentCaptor<ChatMessageEntity> captor = ArgumentCaptor.forClass(ChatMessageEntity.class);
        verify(chatMessageMapper).insert(captor.capture());
        assertEquals(2L, captor.getValue().getSenderId());
        assertEquals(1L, captor.getValue().getReceiverId());
        assertEquals("Can we clarify the delivery scope?", captor.getValue().getContent());
        assertEquals(2L, result.getSenderId());
    }

    @Test
    void sendMessageShouldRejectUnverifiedDeveloperDemandContact() {
        DemandEntity demand = new DemandEntity();
        demand.setId(10L);
        demand.setPublisherId(1L);

        ChatSendDTO dto = new ChatSendDTO();
        dto.setBizType(1);
        dto.setDemandId(10L);
        dto.setReceiverId(1L);
        dto.setContent("Hello");

        when(userBanLifecycleService.normalizeBanStatus(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(demandMapper.selectById(10L)).thenReturn(demand);
        when(userMapper.selectById(1L)).thenReturn(buildUser(1L, "Client", 1, 0));
        when(userMapper.selectById(2L)).thenReturn(buildUser(2L, "Visitor", 1, 0));

        BizException exception = assertThrows(BizException.class, () -> chatService.sendMessage(2L, dto));

        assertEquals(403, exception.getCode());
    }

    @Test
    void listConversationMessagesShouldMarkIncomingMessagesAsRead() {
        DemandEntity demand = new DemandEntity();
        demand.setId(10L);
        demand.setPublisherId(1L);

        ChatMessageEntity message = new ChatMessageEntity();
        message.setId(99L);
        message.setBizType(1);
        message.setDemandId(10L);
        message.setSenderId(1L);
        message.setReceiverId(2L);
        message.setMsgType(1);
        message.setContent("Please share your timeline.");
        message.setReadStatus(0);

        when(userBanLifecycleService.normalizeBanStatus(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(demandMapper.selectById(10L)).thenReturn(demand);
        when(userMapper.selectById(1L)).thenReturn(buildUser(1L, "Client", 1, 0));
        when(userMapper.selectById(2L)).thenReturn(buildUser(2L, "Developer", 1, 2));
        when(chatMessageMapper.selectConversationMessages(1, 10L, null, 2L, 1L)).thenReturn(List.of(message));

        List<ChatMessageVO> messages = chatService.listConversationMessages(2L, 1, 10L, null, 1L);

        verify(chatMessageMapper).markConversationRead(eq(1), eq(10L), isNull(), eq(2L), eq(1L), any());
        assertEquals(1, messages.size());
        assertFalse(Boolean.TRUE.equals(messages.get(0).getSelf()));
        assertEquals("Client", messages.get(0).getSenderNickname());
    }

    @Test
    void recallMessageShouldHideContentForNormalUsersButKeepMessage() {
        ChatMessageEntity original = new ChatMessageEntity();
        original.setId(500L);
        original.setBizType(1);
        original.setDemandId(10L);
        original.setSenderId(2L);
        original.setReceiverId(1L);
        original.setMsgType(1);
        original.setContent("original message");
        original.setStatus(1);

        ChatMessageEntity recalled = new ChatMessageEntity();
        recalled.setId(500L);
        recalled.setBizType(1);
        recalled.setDemandId(10L);
        recalled.setSenderId(2L);
        recalled.setReceiverId(1L);
        recalled.setMsgType(1);
        recalled.setContent("original message");
        recalled.setStatus(2);

        when(chatMessageMapper.selectById(500L)).thenReturn(original, recalled);
        when(chatMessageMapper.updateRecalled(eq(500L), eq(2L), any())).thenReturn(1);
        when(userMapper.selectById(2L)).thenReturn(buildUser(2L, "Developer", 1, 2));
        when(userMapper.selectById(1L)).thenReturn(buildUser(1L, "Client", 1, 0));

        ChatMessageVO recalledMessage = chatService.recallMessage(2L, 500L);

        assertEquals(2, recalledMessage.getStatus());
        assertEquals("message recalled", recalledMessage.getContent());
        assertNull(recalledMessage.getFileUrl());
        verify(chatWebSocketSessionManager).notifyChatMessage(1, 10L, null, 2L, 1L);
    }

    private UserEntity buildUser(Long id, String nickname, Integer status, Integer developerStatus) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setNickname(nickname);
        user.setStatus(status);
        user.setDeveloperStatus(developerStatus);
        user.setAvatarUrl("https://example.com/user.png");
        return user;
    }
}
