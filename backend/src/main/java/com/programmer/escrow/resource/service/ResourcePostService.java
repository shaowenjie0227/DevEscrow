package com.programmer.escrow.resource.service;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.resource.dto.ResourcePostSaveDTO;
import com.programmer.escrow.resource.dto.ResourcePostStatusDTO;
import com.programmer.escrow.resource.entity.ResourcePostEntity;
import com.programmer.escrow.resource.vo.ResourcePostVO;

import java.util.List;

public interface ResourcePostService {
    List<ResourcePostVO> listActive();
    List<ResourcePostVO> listAdmin();
    ResourcePostVO create(Long creatorId, ResourcePostSaveDTO dto);
    ResourcePostVO update(Long id, ResourcePostSaveDTO dto);
    AdminOperationVO updateStatus(Long id, ResourcePostStatusDTO dto);
    ResourcePostEntity getActive(Long id);
    void like(Long id);
    void favorite(Long id);
    void share(Long id);
}
