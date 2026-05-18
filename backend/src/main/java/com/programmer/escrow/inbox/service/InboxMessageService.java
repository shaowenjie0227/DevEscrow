package com.programmer.escrow.inbox.service;

import com.programmer.escrow.admin.dto.AdminUserMessageSendDTO;
import com.programmer.escrow.inbox.vo.InboxMessageVO;
import com.programmer.escrow.inbox.vo.InboxUnreadSummaryVO;

import java.util.List;

public interface InboxMessageService {

    List<InboxMessageVO> listMyMessages(Long userId);

    InboxUnreadSummaryVO getMyUnreadSummary(Long userId);

    InboxMessageVO markRead(Long userId, Long messageId);

    InboxUnreadSummaryVO markAllRead(Long userId);

    InboxMessageVO adminSendMessage(Long adminId, Long userId, AdminUserMessageSendDTO dto);

    InboxMessageVO sendSystemMessage(Long userId, String title, String content, String actionUrl);
}
