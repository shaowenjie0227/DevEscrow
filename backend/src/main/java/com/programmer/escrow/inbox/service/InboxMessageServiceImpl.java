package com.programmer.escrow.inbox.service;

import com.programmer.escrow.admin.dto.AdminUserMessageSendDTO;
import com.programmer.escrow.admin.entity.AdminUserEntity;
import com.programmer.escrow.admin.mapper.AdminUserMapper;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.inbox.entity.InboxMessageEntity;
import com.programmer.escrow.inbox.mapper.InboxMessageMapper;
import com.programmer.escrow.inbox.vo.InboxMessageVO;
import com.programmer.escrow.inbox.vo.InboxUnreadSummaryVO;
import com.programmer.escrow.infra.mail.MailService;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import com.programmer.escrow.user.service.UserBanLifecycleService;
import com.programmer.escrow.websocket.ChatWebSocketSessionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class InboxMessageServiceImpl implements InboxMessageService {

    private static final int STATUS_ACTIVE = 1;
    private static final int USER_STATUS_DELETED = 3;
    private static final int READ_STATUS_UNREAD = 0;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final InboxMessageMapper inboxMessageMapper;
    private final UserMapper userMapper;
    private final AdminUserMapper adminUserMapper;
    private final ChatWebSocketSessionManager chatWebSocketSessionManager;
    private final MailService mailService;
    private final UserBanLifecycleService userBanLifecycleService;

    public InboxMessageServiceImpl(InboxMessageMapper inboxMessageMapper,
                                   UserMapper userMapper,
                                   AdminUserMapper adminUserMapper,
                                   ChatWebSocketSessionManager chatWebSocketSessionManager,
                                   MailService mailService,
                                   UserBanLifecycleService userBanLifecycleService) {
        this.inboxMessageMapper = inboxMessageMapper;
        this.userMapper = userMapper;
        this.adminUserMapper = adminUserMapper;
        this.chatWebSocketSessionManager = chatWebSocketSessionManager;
        this.mailService = mailService;
        this.userBanLifecycleService = userBanLifecycleService;
    }

    @Override
    public List<InboxMessageVO> listMyMessages(Long userId) {
        requireActiveUser(userId);
        Map<Long, AdminUserEntity> adminCache = new HashMap<>();
        return inboxMessageMapper.selectByUserId(userId).stream()
                .map(message -> toVO(message, adminCache))
                .toList();
    }

    @Override
    public InboxUnreadSummaryVO getMyUnreadSummary(Long userId) {
        requireActiveUser(userId);
        return buildUnreadSummary(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InboxMessageVO markRead(Long userId, Long messageId) {
        requireActiveUser(userId);
        InboxMessageEntity message = getMessageOrThrow(messageId);
        if (!Objects.equals(message.getUserId(), userId) || !Objects.equals(message.getStatus(), STATUS_ACTIVE)) {
            throw new BizException(404, "站内信不存在");
        }
        if (Objects.equals(message.getReadStatus(), READ_STATUS_UNREAD)) {
            inboxMessageMapper.updateRead(messageId, userId, LocalDateTime.now());
            message = getMessageOrThrow(messageId);
        }
        return toVO(message, new HashMap<>());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InboxUnreadSummaryVO markAllRead(Long userId) {
        requireActiveUser(userId);
        inboxMessageMapper.updateAllRead(userId, LocalDateTime.now());
        return buildUnreadSummary(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InboxMessageVO adminSendMessage(Long adminId, Long userId, AdminUserMessageSendDTO dto) {
        UserEntity user = requireExistingUser(userId);
        AdminUserEntity admin = adminUserMapper.selectById(adminId);
        if (admin == null || !Objects.equals(admin.getStatus(), STATUS_ACTIVE)) {
            throw new BizException(401, "管理员登录状态已失效");
        }

        InboxMessageEntity entity = new InboxMessageEntity();
        entity.setUserId(user.getId());
        entity.setAdminId(admin.getId());
        entity.setTitle(dto.getTitle().trim());
        entity.setContent(dto.getContent().trim());
        entity.setReadStatus(READ_STATUS_UNREAD);
        entity.setStatus(STATUS_ACTIVE);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        inboxMessageMapper.insert(entity);

        chatWebSocketSessionManager.notifySiteMessage(user.getId(), entity.getId());
        sendInboxMail(user, admin, entity);

        Map<Long, AdminUserEntity> adminCache = new HashMap<>();
        adminCache.put(admin.getId(), admin);
        return toVO(entity, adminCache);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InboxMessageVO sendSystemMessage(Long userId, String title, String content, String actionUrl) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null || Objects.equals(user.getStatus(), USER_STATUS_DELETED)) {
            return null;
        }
        user = userBanLifecycleService.normalizeBanStatus(user);
        if (user.getStatus() == null || user.getStatus() != STATUS_ACTIVE) {
            return null;
        }
        InboxMessageEntity entity = new InboxMessageEntity();
        entity.setUserId(user.getId());
        entity.setAdminId(null);
        entity.setTitle(title.trim());
        entity.setContent(content.trim());
        entity.setActionUrl(StringUtils.hasText(actionUrl) ? actionUrl.trim() : null);
        entity.setReadStatus(READ_STATUS_UNREAD);
        entity.setStatus(STATUS_ACTIVE);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        inboxMessageMapper.insert(entity);

        chatWebSocketSessionManager.notifySiteMessage(user.getId(), entity.getId());
        return toVO(entity, new HashMap<>());
    }

    private InboxUnreadSummaryVO buildUnreadSummary(Long userId) {
        int unreadCount = inboxMessageMapper.countUnreadByUserId(userId);
        InboxMessageEntity latestUnread = unreadCount > 0 ? inboxMessageMapper.selectLatestUnreadByUserId(userId) : null;
        return InboxUnreadSummaryVO.builder()
                .unreadCount(unreadCount)
                .latestTitle(latestUnread == null ? null : latestUnread.getTitle())
                .latestCreatedAt(latestUnread == null ? null : latestUnread.getCreatedAt())
                .build();
    }

    private InboxMessageVO toVO(InboxMessageEntity entity, Map<Long, AdminUserEntity> adminCache) {
        AdminUserEntity admin = loadAdmin(adminCache, entity.getAdminId());
        String senderName = admin == null
                ? "平台通知"
                : (StringUtils.hasText(admin.getRealName()) ? admin.getRealName() : admin.getUsername());
        return InboxMessageVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .adminId(entity.getAdminId())
                .senderName(senderName)
                .title(entity.getTitle())
                .content(entity.getContent())
                .actionUrl(entity.getActionUrl())
                .readStatus(entity.getReadStatus())
                .readAt(entity.getReadAt())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private AdminUserEntity loadAdmin(Map<Long, AdminUserEntity> adminCache, Long adminId) {
        if (adminId == null) {
            return null;
        }
        return adminCache.computeIfAbsent(adminId, adminUserMapper::selectById);
    }

    private InboxMessageEntity getMessageOrThrow(Long messageId) {
        InboxMessageEntity message = inboxMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BizException(404, "站内信不存在");
        }
        return message;
    }

    private UserEntity requireActiveUser(Long userId) {
        UserEntity user = userBanLifecycleService.normalizeBanStatus(requireExistingUser(userId));
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(403, "账号已被禁用");
        }
        return user;
    }

    private UserEntity requireExistingUser(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null || Objects.equals(user.getStatus(), USER_STATUS_DELETED)) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    private void sendInboxMail(UserEntity user, AdminUserEntity admin, InboxMessageEntity entity) {
        if (!StringUtils.hasText(user.getEmail())) {
            return;
        }
        String senderName = StringUtils.hasText(admin.getRealName()) ? admin.getRealName() : admin.getUsername();
        mailService.sendHtml(
                user.getEmail(),
                "【DevEscrow】你收到一条新的管理员通知：" + entity.getTitle(),
                buildInboxMailHtml(user, senderName, entity)
        );
    }

    private String buildInboxMailHtml(UserEntity user, String senderName, InboxMessageEntity entity) {
        String nickname = HtmlUtils.htmlEscape(user.getNickname() == null ? "用户" : user.getNickname());
        String safeSenderName = HtmlUtils.htmlEscape(senderName == null ? "平台通知" : senderName);
        String safeTitle = HtmlUtils.htmlEscape(entity.getTitle());
        String safeContent = HtmlUtils.htmlEscape(entity.getContent()).replace("\n", "<br/>");
        String createdAt = entity.getCreatedAt() == null ? "-" : entity.getCreatedAt().format(DATE_TIME_FORMATTER);
        return """
                <div style="margin:0;padding:32px 0;background:#f3f6fb;font-family:'Segoe UI','PingFang SC','Microsoft YaHei',sans-serif;color:#1f2937;">
                  <div style="width:min(640px,92%%);margin:0 auto;background:#ffffff;border-radius:24px;overflow:hidden;box-shadow:0 24px 60px rgba(15,23,42,0.12);">
                    <div style="padding:28px 32px;background:linear-gradient(135deg,#1d4ed8,#2563eb);color:#ffffff;">
                      <div style="font-size:12px;letter-spacing:.18em;text-transform:uppercase;opacity:.82;">DevEscrow Mailbox</div>
                      <h1 style="margin:14px 0 0;font-size:28px;line-height:1.2;">你收到一条新的管理员通知</h1>
                      <p style="margin:12px 0 0;font-size:14px;line-height:1.8;opacity:.88;">这封邮件和站内信会同步保留，方便你在邮箱和平台内都能查看。</p>
                    </div>
                    <div style="padding:28px 32px 34px;">
                      <p style="margin:0 0 14px;font-size:15px;line-height:1.8;">你好，%s：</p>
                      <div style="display:grid;gap:12px;padding:18px 20px;border-radius:18px;background:#f8fafc;border:1px solid rgba(148,163,184,.18);font-size:14px;line-height:1.9;color:#334155;">
                        <div><strong>发送人：</strong>%s</div>
                        <div><strong>发送时间：</strong>%s</div>
                        <div><strong>标题：</strong>%s</div>
                      </div>
                      <div style="margin-top:18px;padding:18px 20px;border-radius:18px;background:#ffffff;border:1px solid rgba(59,130,246,.14);font-size:14px;line-height:1.9;color:#1f2937;">
                        %s
                      </div>
                    </div>
                  </div>
                </div>
                """.formatted(nickname, safeSenderName, HtmlUtils.htmlEscape(createdAt), safeTitle, safeContent);
    }
}
