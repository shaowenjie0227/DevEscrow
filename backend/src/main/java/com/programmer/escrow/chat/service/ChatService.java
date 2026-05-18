package com.programmer.escrow.chat.service;

import com.programmer.escrow.chat.dto.ChatSendDTO;
import com.programmer.escrow.chat.vo.ChatConversationVO;
import com.programmer.escrow.chat.vo.ChatMessageVO;

import java.util.List;

public interface ChatService {

    List<ChatConversationVO> listMyConversations(Long userId);

    List<ChatMessageVO> listConversationMessages(Long userId,
                                                 Integer bizType,
                                                 Long demandId,
                                                 Long orderId,
                                                 Long partnerId);

    ChatMessageVO sendMessage(Long userId, ChatSendDTO dto);

    ChatMessageVO recallMessage(Long userId, Long messageId);

    List<ChatConversationVO> listAdminConversations(String keyword);

    List<ChatMessageVO> listAdminConversationMessages(Integer bizType,
                                                      Long demandId,
                                                      Long orderId,
                                                      Long clientId,
                                                      Long developerId);
}
