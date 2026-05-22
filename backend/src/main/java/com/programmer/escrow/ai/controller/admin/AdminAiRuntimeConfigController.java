package com.programmer.escrow.ai.controller.admin;

import com.programmer.escrow.ai.dto.AiRuntimeConfigSaveDTO;
import com.programmer.escrow.ai.service.AiRuntimeConfigService;
import com.programmer.escrow.ai.vo.AiRuntimeConfigVO;
import com.programmer.escrow.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ai/runtime-config")
public class AdminAiRuntimeConfigController {

    private final AiRuntimeConfigService aiRuntimeConfigService;

    public AdminAiRuntimeConfigController(AiRuntimeConfigService aiRuntimeConfigService) {
        this.aiRuntimeConfigService = aiRuntimeConfigService;
    }

    @GetMapping
    public ApiResponse<AiRuntimeConfigVO> getConfig() {
        return ApiResponse.success(aiRuntimeConfigService.getAdminConfig());
    }

    @PutMapping
    public ApiResponse<AiRuntimeConfigVO> save(@Valid @RequestBody AiRuntimeConfigSaveDTO dto) {
        return ApiResponse.success(aiRuntimeConfigService.save(dto));
    }
}
