package com.programmer.escrow.admin.service;

import com.programmer.escrow.admin.dto.DemandAuditDTO;
import com.programmer.escrow.admin.dto.UserBanDTO;
import com.programmer.escrow.admin.entity.AdminUserEntity;
import com.programmer.escrow.admin.mapper.AdminUserMapper;
import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.demand.entity.DemandEntity;
import com.programmer.escrow.demand.mapper.DemandMapper;
import com.programmer.escrow.inbox.entity.InboxMessageEntity;
import com.programmer.escrow.inbox.mapper.InboxMessageMapper;
import com.programmer.escrow.infra.mail.MailService;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import com.programmer.escrow.websocket.ChatWebSocketSessionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
public class AdminOpsServiceImpl implements AdminOpsService {

    private static final int USER_STATUS_NORMAL = 1;
    private static final int USER_STATUS_BANNED = 2;
    private static final int ADMIN_STATUS_NORMAL = 1;
    private static final int INBOX_STATUS_ACTIVE = 1;
    private static final int INBOX_UNREAD = 0;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DemandMapper demandMapper;
    private final UserMapper userMapper;
    private final MailService mailService;
    private final AdminUserMapper adminUserMapper;
    private final InboxMessageMapper inboxMessageMapper;
    private final ChatWebSocketSessionManager chatWebSocketSessionManager;

    public AdminOpsServiceImpl(DemandMapper demandMapper,
                               UserMapper userMapper,
                               MailService mailService,
                               AdminUserMapper adminUserMapper,
                               InboxMessageMapper inboxMessageMapper,
                               ChatWebSocketSessionManager chatWebSocketSessionManager) {
        this.demandMapper = demandMapper;
        this.userMapper = userMapper;
        this.mailService = mailService;
        this.adminUserMapper = adminUserMapper;
        this.inboxMessageMapper = inboxMessageMapper;
        this.chatWebSocketSessionManager = chatWebSocketSessionManager;
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
    public AdminOperationVO banUser(Long adminId, Long userId, UserBanDTO dto) {
        AdminUserEntity admin = getAdminOrThrow(adminId);
        UserEntity user = getUserOrThrow(userId);

        int targetStatus = Objects.equals(dto.getStatus(), USER_STATUS_NORMAL) ? USER_STATUS_NORMAL : USER_STATUS_BANNED;
        if (targetStatus == USER_STATUS_BANNED && !StringUtils.hasText(dto.getReason())) {
            throw new BizException(400, "封禁原因不能为空");
        }

        String normalizedReason = StringUtils.hasText(dto.getReason()) ? dto.getReason().trim() : null;
        LocalDateTime banExpiresAt = targetStatus == USER_STATUS_BANNED ? resolveBanExpiresAt(dto.getDays()) : null;

        if (targetStatus == USER_STATUS_BANNED) {
            userMapper.updateBanStatus(userId, USER_STATUS_BANNED, normalizedReason, banExpiresAt);
            notifyBanStatusChanged(admin, user, true, normalizedReason, banExpiresAt, dto.getDays());
        } else {
            userMapper.clearBanStatus(userId);
            notifyBanStatusChanged(admin, user, false, normalizedReason, null, null);
        }

        return AdminOperationVO.builder()
                .targetId(userId)
                .status(targetStatus)
                .message(normalizedReason)
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

    private AdminUserEntity getAdminOrThrow(Long adminId) {
        AdminUserEntity admin = adminUserMapper.selectById(adminId);
        if (admin == null || !Objects.equals(admin.getStatus(), ADMIN_STATUS_NORMAL)) {
            throw new BizException(401, "管理员登录状态已失效");
        }
        return admin;
    }

    private UserEntity getUserOrThrow(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    private LocalDateTime resolveBanExpiresAt(Integer days) {
        if (days == null || days <= 0) {
            return null;
        }
        return LocalDateTime.now().plusDays(days.longValue());
    }

    private void notifyBanStatusChanged(AdminUserEntity admin,
                                        UserEntity user,
                                        boolean banned,
                                        String reason,
                                        LocalDateTime banExpiresAt,
                                        Integer days) {
        String title = banned
                ? (banExpiresAt == null ? "账号已被永久封禁" : "账号已被临时封禁")
                : "账号已解除封禁";
        String content = buildBanStatusContent(banned, reason, banExpiresAt, days);

        InboxMessageEntity entity = new InboxMessageEntity();
        entity.setUserId(user.getId());
        entity.setAdminId(admin.getId());
        entity.setTitle(title);
        entity.setContent(content);
        entity.setReadStatus(INBOX_UNREAD);
        entity.setStatus(INBOX_STATUS_ACTIVE);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        inboxMessageMapper.insert(entity);
        chatWebSocketSessionManager.notifySiteMessage(user.getId(), entity.getId());

        if (StringUtils.hasText(user.getEmail())) {
            mailService.sendHtml(
                    user.getEmail(),
                    "【DevEscrow】" + title,
                    buildBanStatusMailHtml(user, admin, title, content, banned, banExpiresAt, reason)
            );
        }
    }

    private String buildBanStatusContent(boolean banned, String reason, LocalDateTime banExpiresAt, Integer days) {
        if (!banned) {
            return "管理员已解除你的账号封禁，账号现已恢复正常使用。\n"
                    + "如仍有疑问，可通过平台继续与管理员沟通。";
        }

        String durationText = banExpiresAt == null
                ? "永久封禁"
                : ((days == null ? "" : days + " 天") + "，预计解封时间：" + banExpiresAt.format(DATE_TIME_FORMATTER));
        return "管理员已对你的账号执行封禁处理。\n"
                + "封禁时长：" + durationText + "\n"
                + "封禁原因：" + (StringUtils.hasText(reason) ? reason : "-") + "\n"
                + "如有异议，请联系平台管理员。";
    }

    private String buildBanStatusMailHtml(UserEntity user,
                                          AdminUserEntity admin,
                                          String title,
                                          String content,
                                          boolean banned,
                                          LocalDateTime banExpiresAt,
                                          String reason) {
        String nickname = HtmlUtils.htmlEscape(user.getNickname() == null ? "用户" : user.getNickname());
        String senderName = HtmlUtils.htmlEscape(
                StringUtils.hasText(admin.getRealName()) ? admin.getRealName() : admin.getUsername()
        );
        String safeTitle = HtmlUtils.htmlEscape(title);
        String safeContent = HtmlUtils.htmlEscape(content).replace("\n", "<br/>");
        String reasonText = HtmlUtils.htmlEscape(StringUtils.hasText(reason) ? reason : "-");
        String expireText = banExpiresAt == null ? "永久封禁" : HtmlUtils.htmlEscape(banExpiresAt.format(DATE_TIME_FORMATTER));
        String accent = banned ? "#dc2626" : "#0f766e";
        String subCopy = banned ? "你的账号状态发生了变更，请尽快查看说明。" : "管理员已恢复你的账号访问权限。";

        return """
                <div style="margin:0;padding:32px 0;background:#f3f6fb;font-family:'Segoe UI','PingFang SC','Microsoft YaHei',sans-serif;color:#1f2937;">
                  <div style="width:min(640px,92%%);margin:0 auto;background:#ffffff;border-radius:24px;overflow:hidden;box-shadow:0 24px 60px rgba(15,23,42,0.12);">
                    <div style="padding:28px 32px;background:linear-gradient(135deg,%s,%s);color:#ffffff;">
                      <div style="font-size:12px;letter-spacing:.18em;text-transform:uppercase;opacity:.82;">DevEscrow Notice</div>
                      <h1 style="margin:14px 0 0;font-size:28px;line-height:1.2;">%s</h1>
                      <p style="margin:12px 0 0;font-size:14px;line-height:1.8;opacity:.88;">%s</p>
                    </div>
                    <div style="padding:28px 32px 34px;">
                      <p style="margin:0 0 14px;font-size:15px;line-height:1.8;">你好，%s：</p>
                      <div style="display:grid;gap:12px;padding:18px 20px;border-radius:18px;background:#f8fafc;border:1px solid rgba(148,163,184,.18);font-size:14px;line-height:1.9;color:#334155;">
                        <div><strong>处理人：</strong>%s</div>
                        <div><strong>封禁原因：</strong>%s</div>
                        <div><strong>%s：</strong>%s</div>
                      </div>
                      <div style="margin-top:18px;padding:18px 20px;border-radius:18px;background:#ffffff;border:1px solid rgba(15,23,42,.08);font-size:14px;line-height:1.9;color:#1f2937;">
                        %s
                      </div>
                    </div>
                  </div>
                </div>
                """.formatted(
                accent,
                banned ? "#f97316" : "#14b8a6",
                safeTitle,
                subCopy,
                nickname,
                senderName,
                reasonText,
                banned ? "解封时间" : "当前状态",
                banned ? expireText : "已恢复正常",
                safeContent
        );
    }

    private void sendDeveloperMail(UserEntity user, Integer approve, String remark, String subjectPrefix) {
        if (!StringUtils.hasText(user.getEmail())) {
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
