package com.programmer.escrow.admin.service;

import com.programmer.escrow.admin.dto.DemandAuditDTO;
import com.programmer.escrow.admin.dto.UserBanDTO;
import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.demand.entity.DemandEntity;
import com.programmer.escrow.demand.mapper.DemandMapper;
import com.programmer.escrow.infra.mail.MailService;
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
    private final MailService mailService;

    public AdminOpsServiceImpl(DemandMapper demandMapper, UserMapper userMapper, MailService mailService) {
        this.demandMapper = demandMapper;
        this.userMapper = userMapper;
        this.mailService = mailService;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO auditDeveloper(Long userId, Integer approve, String remark) {
        UserEntity user = getUserOrThrow(userId);
        user.setDeveloperStatus(Objects.equals(approve, 1) ? 2 : 3);
        user.setIdVerifyStatus(Objects.equals(approve, 1) ? 2 : 3);
        user.setSkillAuditStatus(Objects.equals(approve, 1) ? 2 : 3);
        user.setSkillAuditReason(remark);
        userMapper.updateDeveloperProfile(user);
        sendDeveloperMail(user, approve, remark, "开发者资料审核");
        return AdminOperationVO.builder()
                .targetId(userId)
                .status(user.getDeveloperStatus())
                .message(remark)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO auditDeveloperSkillTags(Long userId, Integer approve, String remark) {
        UserEntity user = getUserOrThrow(userId);
        user.setSkillAuditStatus(Objects.equals(approve, 1) ? 2 : 3);
        user.setSkillAuditReason(remark);
        if (Objects.equals(approve, 1)) {
            user.setDeveloperStatus(2);
        }
        userMapper.updateDeveloperProfile(user);
        sendDeveloperMail(user, approve, remark, "技术栈审核");
        return AdminOperationVO.builder()
                .targetId(userId)
                .status(user.getSkillAuditStatus())
                .message(remark)
                .build();
    }

    private UserEntity getUserOrThrow(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    private void sendDeveloperMail(UserEntity user, Integer approve, String remark, String subjectPrefix) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }
        String subject = subjectPrefix + (Objects.equals(approve, 1) ? "通过" : "驳回");
        String content = "你好，" + user.getNickname() + "：\n\n"
                + subjectPrefix + (Objects.equals(approve, 1) ? "已通过" : "未通过") + "。\n"
                + "备注：" + (remark == null ? "-" : remark) + "\n\n"
                + "如需重新提交，请登录平台继续操作。\n";
        mailService.sendText(user.getEmail(), subject, content);
    }
}
