package com.programmer.escrow.admin.service;

import com.programmer.escrow.admin.dto.DemandAuditDTO;
import com.programmer.escrow.admin.dto.UserBanDTO;
import com.programmer.escrow.admin.vo.AdminOperationVO;

public interface AdminOpsService {

    AdminOperationVO auditDemand(Long demandId, DemandAuditDTO dto);

    AdminOperationVO banUser(Long userId, UserBanDTO dto);
}
