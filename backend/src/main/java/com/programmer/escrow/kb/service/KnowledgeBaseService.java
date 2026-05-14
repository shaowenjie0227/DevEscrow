package com.programmer.escrow.kb.service;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.kb.dto.KnowledgeBaseSaveDTO;
import com.programmer.escrow.kb.dto.KnowledgeBaseStatusDTO;
import com.programmer.escrow.kb.entity.KnowledgeBaseEntity;
import com.programmer.escrow.kb.vo.KnowledgeBaseVO;

import java.util.List;

public interface KnowledgeBaseService {
    List<KnowledgeBaseVO> listActive();
    List<KnowledgeBaseVO> listAdmin();
    KnowledgeBaseVO create(KnowledgeBaseSaveDTO dto);
    KnowledgeBaseVO update(Long id, KnowledgeBaseSaveDTO dto);
    AdminOperationVO updateStatus(Long id, KnowledgeBaseStatusDTO dto);
    KnowledgeBaseEntity getActive(Long id);
}
