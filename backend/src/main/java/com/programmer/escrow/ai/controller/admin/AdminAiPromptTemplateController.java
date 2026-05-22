package com.programmer.escrow.ai.controller.admin;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.ai.dto.AiPromptTemplateSaveDTO;
import com.programmer.escrow.ai.dto.AiPromptTemplateStatusDTO;
import com.programmer.escrow.ai.service.AiPromptTemplateService;
import com.programmer.escrow.ai.vo.AiPromptTemplateVO;
import com.programmer.escrow.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ai/prompt-templates")
public class AdminAiPromptTemplateController {

    private final AiPromptTemplateService aiPromptTemplateService;

    public AdminAiPromptTemplateController(AiPromptTemplateService aiPromptTemplateService) {
        this.aiPromptTemplateService = aiPromptTemplateService;
    }

    @GetMapping
    public ApiResponse<List<AiPromptTemplateVO>> list() {
        return ApiResponse.success(aiPromptTemplateService.listAdmin());
    }

    @PostMapping
    public ApiResponse<AiPromptTemplateVO> create(@Valid @RequestBody AiPromptTemplateSaveDTO dto) {
        return ApiResponse.success(aiPromptTemplateService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<AiPromptTemplateVO> update(@PathVariable Long id, @Valid @RequestBody AiPromptTemplateSaveDTO dto) {
        return ApiResponse.success(aiPromptTemplateService.update(id, dto));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<AdminOperationVO> updateStatus(@PathVariable Long id, @Valid @RequestBody AiPromptTemplateStatusDTO dto) {
        return ApiResponse.success(aiPromptTemplateService.updateStatus(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<AdminOperationVO> delete(@PathVariable Long id) {
        return ApiResponse.success(aiPromptTemplateService.delete(id));
    }
}
