package com.programmer.escrow.chat.controller;

import com.programmer.escrow.chat.service.ChatAttachmentService;
import com.programmer.escrow.chat.vo.ChatAttachmentUploadVO;
import com.programmer.escrow.common.api.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chat/uploads")
public class ChatAttachmentController {

    private final ChatAttachmentService chatAttachmentService;

    public ChatAttachmentController(ChatAttachmentService chatAttachmentService) {
        this.chatAttachmentService = chatAttachmentService;
    }

    @PostMapping("/attachments")
    public ApiResponse<ChatAttachmentUploadVO> uploadAttachment(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(chatAttachmentService.uploadAttachment(file));
    }
}
