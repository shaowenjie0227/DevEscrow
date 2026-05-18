package com.programmer.escrow.inbox.controller;

import com.programmer.escrow.auth.context.UserContextHolder;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.inbox.service.InboxMessageService;
import com.programmer.escrow.inbox.vo.InboxMessageVO;
import com.programmer.escrow.inbox.vo.InboxUnreadSummaryVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inbox/messages")
public class InboxMessageController {

    private final InboxMessageService inboxMessageService;

    public InboxMessageController(InboxMessageService inboxMessageService) {
        this.inboxMessageService = inboxMessageService;
    }

    @GetMapping
    public ApiResponse<List<InboxMessageVO>> listMyMessages() {
        return ApiResponse.success(inboxMessageService.listMyMessages(UserContextHolder.getRequiredUserId()));
    }

    @GetMapping("/unread-summary")
    public ApiResponse<InboxUnreadSummaryVO> getUnreadSummary() {
        return ApiResponse.success(inboxMessageService.getMyUnreadSummary(UserContextHolder.getRequiredUserId()));
    }

    @PostMapping("/{messageId}/read")
    public ApiResponse<InboxMessageVO> markRead(@PathVariable Long messageId) {
        return ApiResponse.success(inboxMessageService.markRead(UserContextHolder.getRequiredUserId(), messageId));
    }

    @PostMapping("/read-all")
    public ApiResponse<InboxUnreadSummaryVO> markAllRead() {
        return ApiResponse.success(inboxMessageService.markAllRead(UserContextHolder.getRequiredUserId()));
    }
}
