package com.programmer.escrow.admin.service;

import com.programmer.escrow.admin.dto.DemandAuditDTO;
import com.programmer.escrow.admin.dto.UserBanDTO;
import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.demand.entity.DemandEntity;
import com.programmer.escrow.demand.mapper.DemandMapper;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class AdminOpsServiceImpl implements AdminOpsService {

    private final DemandMapper demandMapper;
    private final UserMapper userMapper;

    public AdminOpsServiceImpl(DemandMapper demandMapper, UserMapper userMapper) {
        this.demandMapper = demandMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO auditDemand(Long demandId, DemandAuditDTO dto) {
        DemandEntity demand = demandMapper.selectById(demandId);
        if (demand == null) {
            throw new BizException(404, "需求不存在");
        }
        if (!Objects.equals(demand.getReviewStatus(), 0)) {
            throw new BizException(400, "该需求当前不是待审核状态");
        }

        Integer targetStatus = Objects.equals(dto.getReviewStatus(), 1) ? 2 : 8;
        LocalDateTime publishAt = Objects.equals(dto.getReviewStatus(), 1) ? LocalDateTime.now() : null;
        String rejectReason = Objects.equals(dto.getReviewStatus(), 2) ? dto.getRemark() : null;
        demandMapper.updateAuditStatus(demandId, dto.getReviewStatus(), targetStatus, publishAt, rejectReason);
        return AdminOperationVO.builder()
                .targetId(demandId)
                .status(targetStatus)
                .message(dto.getRemark())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO banUser(Long userId, UserBanDTO dto) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        userMapper.updateStatus(userId, 2);
        return AdminOperationVO.builder()
                .targetId(userId)
                .status(2)
                .message(dto.getReason())
                .build();
    }
}
