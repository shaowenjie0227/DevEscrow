package com.programmer.escrow.chat.controller;

import com.programmer.escrow.auth.context.UserContextHolder;
import com.programmer.escrow.chat.dto.ChatSendDTO;
import com.programmer.escrow.chat.service.ChatService;
import com.programmer.escrow.chat.vo.ChatConversationVO;
import com.programmer.escrow.chat.vo.ChatMessageVO;
import com.programmer.escrow.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/conversations")
    public ApiResponse<List<ChatConversationVO>> listConversations() {
        return ApiResponse.success(chatService.listMyConversations(UserContextHolder.getRequiredUserId()));
    }

    @GetMapping("/messages")
    public ApiResponse<List<ChatMessageVO>> listMessages(@RequestParam Integer bizType,
                                                         @RequestParam(required = false) Long demandId,
                                                         @RequestParam(required = false) Long orderId,
                                                         @RequestParam Long partnerId) {
        return ApiResponse.success(chatService.listConversationMessages(
                UserContextHolder.getRequiredUserId(),
                bizType,
                demandId,
                orderId,
                partnerId
        ));
    }

    @PostMapping("/messages")
    public ApiResponse<ChatMessageVO> sendMessage(@Valid @RequestBody ChatSendDTO dto) {
        return ApiResponse.success(chatService.sendMessage(UserContextHolder.getRequiredUserId(), dto));
    }

    @PostMapping("/messages/{messageId}/recall")
    public ApiResponse<ChatMessageVO> recallMessage(@PathVariable Long messageId) {
        return ApiResponse.success(chatService.recallMessage(UserContextHolder.getRequiredUserId(), messageId));
    }
}
