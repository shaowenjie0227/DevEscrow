package com.programmer.escrow.skill.service;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.skill.dto.SkillTagSaveDTO;
import com.programmer.escrow.skill.dto.SkillTagStatusDTO;
import com.programmer.escrow.skill.entity.SkillTagEntity;
import com.programmer.escrow.skill.vo.SkillTagVO;

import java.util.List;

public interface SkillTagService {
    List<SkillTagVO> listActive();
    List<SkillTagVO> listAdmin();
    SkillTagVO create(SkillTagSaveDTO dto);
    SkillTagVO update(Long tagId, SkillTagSaveDTO dto);
    AdminOperationVO updateStatus(Long tagId, SkillTagStatusDTO dto);
    SkillTagEntity getActiveTag(Long tagId);
}
