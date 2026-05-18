package com.programmer.escrow.user.service;

import com.programmer.escrow.inbox.entity.InboxMessageEntity;
import com.programmer.escrow.inbox.mapper.InboxMessageMapper;
import com.programmer.escrow.infra.mail.MailService;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import com.programmer.escrow.websocket.ChatWebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class UserBanLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(UserBanLifecycleService.class);
    private static final int USER_STATUS_NORMAL = 1;
    private static final int USER_STATUS_BANNED = 2;
    private static final int INBOX_STATUS_ACTIVE = 1;
    private static final int INBOX_UNREAD = 0;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserMapper userMapper;
    private final InboxMessageMapper inboxMessageMapper;
    private final MailService mailService;
    private final ChatWebSocketSessionManager chatWebSocketSessionManager;

    public UserBanLifecycleService(UserMapper userMapper,
                                   InboxMessageMapper inboxMessageMapper,
                                   MailService mailService,
                                   ChatWebSocketSessionManager chatWebSocketSessionManager) {
        this.userMapper = userMapper;
        this.inboxMessageMapper = inboxMessageMapper;
        this.mailService = mailService;
        this.chatWebSocketSessionManager = chatWebSocketSessionManager;
    }

    public UserEntity normalizeBanStatus(UserEntity user) {
        if (!isExpiredTemporaryBan(user)) {
            return user;
        }
        int updated = userMapper.releaseExpiredBanById(user.getId());
        if (updated <= 0) {
            return userMapper.selectById(user.getId());
        }
        UserEntity refreshed = userMapper.selectById(user.getId());
        notifyAutoUnbanned(user);
        return refreshed;
    }

    @Scheduled(fixedDelay = 60_000L, initialDelay = 60_000L)
    public void releaseExpiredBans() {
        List<UserEntity> expiredUsers = userMapper.selectExpiredBannedUsers();
        for (UserEntity user : expiredUsers) {
            if (userMapper.releaseExpiredBanById(user.getId()) > 0) {
                notifyAutoUnbanned(user);
            }
        }
    }

    public boolean isExpiredTemporaryBan(UserEntity user) {
        return user != null
                && Objects.equals(user.getStatus(), USER_STATUS_BANNED)
                && user.getBanExpiresAt() != null
                && !user.getBanExpiresAt().isAfter(LocalDateTime.now());
    }

    private void notifyAutoUnbanned(UserEntity user) {
        try {
            InboxMessageEntity entity = new InboxMessageEntity();
            entity.setUserId(user.getId());
            entity.setAdminId(null);
            entity.setTitle("账号封禁已到期，系统已自动解封");
            entity.setContent(buildAutoUnbanContent(user));
            entity.setReadStatus(INBOX_UNREAD);
            entity.setStatus(INBOX_STATUS_ACTIVE);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            inboxMessageMapper.insert(entity);
            chatWebSocketSessionManager.notifySiteMessage(user.getId(), entity.getId());

            if (StringUtils.hasText(user.getEmail())) {
                mailService.sendHtml(
                        user.getEmail(),
                        "【DevEscrow】账号已自动解封",
                        buildAutoUnbanMailHtml(user)
                );
            }
        } catch (Exception ex) {
            log.warn("Failed to notify auto unban for user {}", user == null ? null : user.getId(), ex);
        }
    }

    private String buildAutoUnbanContent(UserEntity user) {
        String expireText = user.getBanExpiresAt() == null ? "-" : user.getBanExpiresAt().format(DATE_TIME_FORMATTER);
        return "你的账号封禁时间已到，系统已自动恢复账号访问权限。\n"
                + "解封时间：" + expireText + "\n"
                + "现在可以重新登录并继续使用平台。";
    }

    private String buildAutoUnbanMailHtml(UserEntity user) {
        String nickname = HtmlUtils.htmlEscape(user.getNickname() == null ? "用户" : user.getNickname());
        String expireText = user.getBanExpiresAt() == null ? "-" : user.getBanExpiresAt().format(DATE_TIME_FORMATTER);
        return """
                <div style="margin:0;padding:32px 0;background:#f3f6fb;font-family:'Segoe UI','PingFang SC','Microsoft YaHei',sans-serif;color:#1f2937;">
                  <div style="width:min(640px,92%%);margin:0 auto;background:#ffffff;border-radius:24px;overflow:hidden;box-shadow:0 24px 60px rgba(15,23,42,0.12);">
                    <div style="padding:28px 32px;background:linear-gradient(135deg,#0f766e,#14b8a6);color:#ffffff;">
                      <div style="font-size:12px;letter-spacing:.18em;text-transform:uppercase;opacity:.82;">DevEscrow</div>
                      <h1 style="margin:14px 0 0;font-size:28px;line-height:1.2;">你的账号已自动解封</h1>
                      <p style="margin:12px 0 0;font-size:14px;line-height:1.8;opacity:.88;">封禁到期后，系统已自动恢复访问权限。</p>
                    </div>
                    <div style="padding:28px 32px 34px;">
                      <p style="margin:0 0 14px;font-size:15px;line-height:1.8;">你好，%s：</p>
                      <div style="padding:18px 20px;border-radius:18px;background:#f8fafc;border:1px solid rgba(148,163,184,.18);font-size:14px;line-height:1.9;color:#334155;">
                        <div><strong>解封时间：</strong>%s</div>
                        <div style="margin-top:8px;">你的账号已恢复正常状态，现在可以重新登录并继续使用平台。</div>
                      </div>
                    </div>
                  </div>
                </div>
                """.formatted(nickname, HtmlUtils.htmlEscape(expireText));
    }
}
