package com.programmer.escrow.ai.controller.client;

import com.programmer.escrow.ai.dto.AiDemandDraftGenerateDTO;
import com.programmer.escrow.ai.service.AiDemandDraftService;
import com.programmer.escrow.ai.vo.AiDemandDraftVO;
import com.programmer.escrow.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/client/ai")
public class ClientAiDemandController {

    private final AiDemandDraftService aiDemandDraftService;

    public ClientAiDemandController(AiDemandDraftService aiDemandDraftService) {
        this.aiDemandDraftService = aiDemandDraftService;
    }

    @PostMapping("/demand-draft")
    public ApiResponse<AiDemandDraftVO> generateDemandDraft(@Valid @RequestBody AiDemandDraftGenerateDTO dto) {
        return ApiResponse.success(aiDemandDraftService.generateDraft(dto));
    }

    @PostMapping(value = "/demand-draft/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamDemandDraft(@Valid @RequestBody AiDemandDraftGenerateDTO dto) {
        return aiDemandDraftService.streamDraft(dto);
    }
}
