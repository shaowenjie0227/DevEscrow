package com.programmer.escrow.kb.controller;

import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.kb.service.KnowledgeBaseService;
import com.programmer.escrow.kb.vo.KnowledgeBaseVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-bases")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @GetMapping
    public ApiResponse<List<KnowledgeBaseVO>> listActive() {
        return ApiResponse.success(knowledgeBaseService.listActive());
    }
}
