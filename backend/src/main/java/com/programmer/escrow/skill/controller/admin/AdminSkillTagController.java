package com.programmer.escrow.skill.controller.admin;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.skill.dto.SkillTagSaveDTO;
import com.programmer.escrow.skill.dto.SkillTagStatusDTO;
import com.programmer.escrow.skill.service.SkillTagService;
import com.programmer.escrow.skill.vo.SkillTagVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/skill-tags")
public class AdminSkillTagController {

    private final SkillTagService skillTagService;

    public AdminSkillTagController(SkillTagService skillTagService) {
        this.skillTagService = skillTagService;
    }

    @GetMapping
    public ApiResponse<List<SkillTagVO>> listAdmin() {
        return ApiResponse.success(skillTagService.listAdmin());
    }

    @PostMapping
    public ApiResponse<SkillTagVO> create(@Valid @RequestBody SkillTagSaveDTO dto) {
        return ApiResponse.success(skillTagService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<SkillTagVO> update(@PathVariable Long id, @Valid @RequestBody SkillTagSaveDTO dto) {
        return ApiResponse.success(skillTagService.update(id, dto));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<AdminOperationVO> updateStatus(@PathVariable Long id, @Valid @RequestBody SkillTagStatusDTO dto) {
        return ApiResponse.success(skillTagService.updateStatus(id, dto));
    }
}
