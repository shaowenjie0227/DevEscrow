package com.programmer.escrow.skill.controller;

import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.skill.service.SkillTagService;
import com.programmer.escrow.skill.vo.SkillTagVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/skill-tags")
public class SkillTagController {

    private final SkillTagService skillTagService;

    public SkillTagController(SkillTagService skillTagService) {
        this.skillTagService = skillTagService;
    }

    @GetMapping
    public ApiResponse<List<SkillTagVO>> listActive() {
        return ApiResponse.success(skillTagService.listActive());
    }
}
