package com.programmer.escrow.chat.controller.admin;

import com.programmer.escrow.chat.service.ChatService;
import com.programmer.escrow.chat.vo.ChatConversationVO;
import com.programmer.escrow.chat.vo.ChatMessageVO;
import com.programmer.escrow.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/chats")
public class AdminChatAuditController {

    private final ChatService chatService;

    public AdminChatAuditController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/conversations")
    public ApiResponse<List<ChatConversationVO>> listConversations(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(chatService.listAdminConversations(keyword));
    }

    @GetMapping("/messages")
    public ApiResponse<List<ChatMessageVO>> listMessages(@RequestParam Integer bizType,
                                                         @RequestParam(required = false) Long demandId,
                                                         @RequestParam(required = false) Long orderId,
                                                         @RequestParam Long clientId,
                                                         @RequestParam Long developerId) {
        return ApiResponse.success(chatService.listAdminConversationMessages(
                bizType,
                demandId,
                orderId,
                clientId,
                developerId
        ));
    }
}
