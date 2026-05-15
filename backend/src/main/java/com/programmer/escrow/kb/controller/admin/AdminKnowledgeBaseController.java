package com.programmer.escrow.kb.controller.admin;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.kb.dto.KnowledgeBaseSaveDTO;
import com.programmer.escrow.kb.dto.KnowledgeBaseStatusDTO;
import com.programmer.escrow.kb.service.KnowledgeBaseService;
import com.programmer.escrow.kb.vo.KnowledgeBaseVO;
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
@RequestMapping("/api/admin/knowledge-bases")
public class AdminKnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public AdminKnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @GetMapping
    public ApiResponse<List<KnowledgeBaseVO>> listAdmin() {
        return ApiResponse.success(knowledgeBaseService.listAdmin());
    }

    @PostMapping
    public ApiResponse<KnowledgeBaseVO> create(@Valid @RequestBody KnowledgeBaseSaveDTO dto) {
        return ApiResponse.success(knowledgeBaseService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<KnowledgeBaseVO> update(@PathVariable Long id, @Valid @RequestBody KnowledgeBaseSaveDTO dto) {
        return ApiResponse.success(knowledgeBaseService.update(id, dto));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<AdminOperationVO> updateStatus(@PathVariable Long id, @Valid @RequestBody KnowledgeBaseStatusDTO dto) {
        return ApiResponse.success(knowledgeBaseService.updateStatus(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<AdminOperationVO> delete(@PathVariable Long id) {
        return ApiResponse.success(knowledgeBaseService.delete(id));
    }
}
