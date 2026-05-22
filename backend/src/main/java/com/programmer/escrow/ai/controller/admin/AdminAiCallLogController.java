package com.programmer.escrow.ai.controller.admin;

import com.programmer.escrow.ai.service.AiCallLogService;
import com.programmer.escrow.ai.vo.AiCallLogVO;
import com.programmer.escrow.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ai/call-logs")
public class AdminAiCallLogController {

    private final AiCallLogService aiCallLogService;

    public AdminAiCallLogController(AiCallLogService aiCallLogService) {
        this.aiCallLogService = aiCallLogService;
    }

    @GetMapping
    public ApiResponse<List<AiCallLogVO>> listRecent(@RequestParam(required = false) String sceneCode,
                                                     @RequestParam(required = false) Integer status,
                                                     @RequestParam(required = false) Integer limit) {
        return ApiResponse.success(aiCallLogService.listRecent(sceneCode, status, limit));
    }
}
