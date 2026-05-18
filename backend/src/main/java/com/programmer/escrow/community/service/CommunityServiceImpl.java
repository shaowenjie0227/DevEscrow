package com.programmer.escrow.community.service;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.community.dto.CommunityPostCreateDTO;
import com.programmer.escrow.community.dto.CommunityReplyCreateDTO;
import com.programmer.escrow.community.dto.CommunityStatusDTO;
import com.programmer.escrow.community.entity.CommunityPostActionEntity;
import com.programmer.escrow.community.entity.CommunityPostEntity;
import com.programmer.escrow.community.entity.CommunityReplyEntity;
import com.programmer.escrow.community.mapper.CommunityPostActionMapper;
import com.programmer.escrow.community.mapper.CommunityPostMapper;
import com.programmer.escrow.community.mapper.CommunityReplyMapper;
import com.programmer.escrow.community.vo.CommunityPostVO;
import com.programmer.escrow.community.vo.CommunityReplyVO;
import com.programmer.escrow.inbox.service.InboxMessageService;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@Service
public class CommunityServiceImpl implements CommunityService {

    private static final int STATUS_ENABLED = 1;
    private static final int STATUS_DISABLED = 2;
    private static final int ACTION_LIKE = 1;
    private static final int ACTION_FAVORITE = 2;
    private static final int POST_CONTENT_MIN_LENGTH = 10;
    private static final int POST_CONTENT_MAX_LENGTH = 10000;
    private static final int REPLY_NOTIFICATION_PREVIEW_MAX_LENGTH = 80;
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[a-zA-Z/][^>]*>");
    private static final Safelist COMMUNITY_CONTENT_SAFELIST = Safelist.none()
            .addTags(
                    "p", "br", "strong", "b", "em", "i", "u", "s",
                    "blockquote", "pre", "code", "ul", "ol", "li",
                    "h1", "h2", "h3", "h4", "h5", "h6", "a", "span", "div"
            )
            .addAttributes("a", "href", "target", "rel")
            .addAttributes("p", "class")
            .addAttributes("span", "class")
            .addAttributes("div", "class")
            .addAttributes("pre", "class")
            .addAttributes("code", "class")
            .addProtocols("a", "href", "http", "https", "mailto");

    private final CommunityPostMapper communityPostMapper;
    private final CommunityReplyMapper communityReplyMapper;
    private final CommunityPostActionMapper communityPostActionMapper;
    private final UserMapper userMapper;
    private final InboxMessageService inboxMessageService;

    public CommunityServiceImpl(CommunityPostMapper communityPostMapper,
                                CommunityReplyMapper communityReplyMapper,
                                CommunityPostActionMapper communityPostActionMapper,
                                UserMapper userMapper,
                                InboxMessageService inboxMessageService) {
        this.communityPostMapper = communityPostMapper;
        this.communityReplyMapper = communityReplyMapper;
        this.communityPostActionMapper = communityPostActionMapper;
        this.userMapper = userMapper;
        this.inboxMessageService = inboxMessageService;
    }

    @Override
    public List<CommunityPostVO> listPosts(String sortType, String forumName, String keyword, Boolean mine, Long currentUserId) {
        if (Boolean.TRUE.equals(mine) && currentUserId == null) {
            throw new BizException(401, "请先登录后查看我的帖子");
        }
        List<CommunityPostEntity> entities = communityPostMapper.selectList(
                normalizeForumName(forumName),
                normalizeKeyword(keyword),
                Boolean.TRUE.equals(mine) ? currentUserId : null,
                STATUS_ENABLED
        );
        List<CommunityPostEntity> sorted = sortPosts(entities, sortType);
        return toPostVOList(sorted, currentUserId);
    }

    @Override
    public CommunityPostVO getPost(Long postId, Long currentUserId) {
        CommunityPostEntity entity = getActivePostOrThrow(postId);
        return toPostVOList(List.of(entity), currentUserId).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityPostVO createPost(Long userId, CommunityPostCreateDTO dto) {
        UserEntity user = getUserOrThrow(userId);
        CommunityPostEntity entity = new CommunityPostEntity();
        String content = sanitizePostContent(dto.getContent());
        String plainContent = extractPlainText(content);
        entity.setCreatorId(userId);
        entity.setAuthorName(resolveAuthorName(user));
        entity.setForumName(dto.getForumName().trim());
        entity.setTitle(dto.getTitle().trim());
        entity.setSummary(resolveSummary(dto.getSummary(), plainContent));
        entity.setContent(content);
        entity.setReplyCount(0);
        entity.setLikeCount(0);
        entity.setFavoriteCount(0);
        entity.setStatus(STATUS_ENABLED);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        communityPostMapper.insert(entity);
        return toPostVOList(List.of(entity), userId).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityPostVO toggleLikePost(Long userId, Long postId) {
        getActivePostOrThrow(postId);
        getUserOrThrow(userId);
        togglePostAction(userId, postId, ACTION_LIKE);
        return getPost(postId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityPostVO toggleFavoritePost(Long userId, Long postId) {
        getActivePostOrThrow(postId);
        getUserOrThrow(userId);
        togglePostAction(userId, postId, ACTION_FAVORITE);
        return getPost(postId, userId);
    }

    @Override
    public List<CommunityReplyVO> listReplies(Long postId) {
        getActivePostOrThrow(postId);
        return communityReplyMapper.selectList(postId, STATUS_ENABLED).stream().map(this::toReplyVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityReplyVO createReply(Long userId, Long postId, CommunityReplyCreateDTO dto) {
        CommunityPostEntity post = getActivePostOrThrow(postId);
        UserEntity user = getUserOrThrow(userId);
        CommunityReplyEntity parentReply = resolveParentReply(postId, dto.getParentReplyId());
        CommunityReplyEntity entity = new CommunityReplyEntity();
        entity.setPostId(postId);
        entity.setCreatorId(userId);
        entity.setParentReplyId(parentReply == null ? null : parentReply.getId());
        entity.setAuthorName(resolveAuthorName(user));
        entity.setReplyToAuthorName(parentReply == null ? null : parentReply.getAuthorName());
        entity.setContent(dto.getContent().trim());
        entity.setLikeCount(0);
        entity.setStatus(STATUS_ENABLED);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        communityReplyMapper.insert(entity);
        refreshReplyCount(postId);
        notifyReplyParticipants(post, parentReply, entity);
        return toReplyVO(entity);
    }

    @Override
    public List<CommunityPostVO> listAdminPosts(String forumName, String keyword, Integer status) {
        return toPostVOList(
                communityPostMapper.selectList(normalizeForumName(forumName), normalizeKeyword(keyword), null, normalizeStatusFilter(status)),
                null
        );
    }

    @Override
    public List<CommunityReplyVO> listAdminReplies(Long postId, Integer status) {
        getPostOrThrow(postId);
        return communityReplyMapper.selectList(postId, normalizeStatusFilter(status)).stream().map(this::toReplyVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO updatePostStatus(Long postId, CommunityStatusDTO dto) {
        CommunityPostEntity entity = getPostOrThrow(postId);
        int targetStatus = normalizeRequiredStatus(dto.getStatus());
        if (!Objects.equals(entity.getStatus(), targetStatus)) {
            communityPostMapper.updateStatus(postId, targetStatus);
        }
        return AdminOperationVO.builder()
                .targetId(postId)
                .status(targetStatus)
                .message(targetStatus == STATUS_ENABLED ? "帖子已启用" : "帖子已下架")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO deletePost(Long postId) {
        getPostOrThrow(postId);
        communityReplyMapper.deleteByPostId(postId);
        communityPostActionMapper.deleteByPostId(postId);
        communityPostMapper.deleteById(postId);
        return AdminOperationVO.builder()
                .targetId(postId)
                .status(0)
                .message("帖子已删除")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO updateReplyStatus(Long replyId, CommunityStatusDTO dto) {
        CommunityReplyEntity entity = getReplyOrThrow(replyId);
        int targetStatus = normalizeRequiredStatus(dto.getStatus());
        if (!Objects.equals(entity.getStatus(), targetStatus)) {
            communityReplyMapper.updateStatus(replyId, targetStatus);
            refreshReplyCount(entity.getPostId());
        }
        return AdminOperationVO.builder()
                .targetId(replyId)
                .status(targetStatus)
                .message(targetStatus == STATUS_ENABLED ? "回复已启用" : "回复已下架")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationVO deleteReply(Long replyId) {
        CommunityReplyEntity entity = getReplyOrThrow(replyId);
        communityReplyMapper.deleteById(replyId);
        refreshReplyCount(entity.getPostId());
        return AdminOperationVO.builder()
                .targetId(replyId)
                .status(0)
                .message("回复已删除")
                .build();
    }

    private List<CommunityPostEntity> sortPosts(List<CommunityPostEntity> entities, String sortType) {
        Comparator<CommunityPostEntity> latestComparator = Comparator
                .comparing(CommunityPostEntity::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(CommunityPostEntity::getId, Comparator.nullsLast(Comparator.reverseOrder()));

        if ("hot".equalsIgnoreCase(sortType)) {
            return entities.stream()
                    .sorted(Comparator
                            .comparingInt(this::calculateHotScore)
                            .reversed()
                            .thenComparing(CommunityPostEntity::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
        }
        if ("unanswered".equalsIgnoreCase(sortType)) {
            return entities.stream()
                    .filter(item -> safe(item.getReplyCount()) == 0)
                    .sorted(latestComparator)
                    .toList();
        }
        return entities.stream().sorted(latestComparator).toList();
    }

    private int calculateHotScore(CommunityPostEntity entity) {
        return safe(entity.getLikeCount()) * 2
                + safe(entity.getFavoriteCount()) * 3
                + safe(entity.getReplyCount()) * 4;
    }

    private List<CommunityPostVO> toPostVOList(List<CommunityPostEntity> entities, Long currentUserId) {
        if (entities.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> postIds = entities.stream().map(CommunityPostEntity::getId).toList();
        Set<Long> likedPostIds = findActiveActionPostIds(currentUserId, postIds, ACTION_LIKE);
        Set<Long> favoritedPostIds = findActiveActionPostIds(currentUserId, postIds, ACTION_FAVORITE);
        return entities.stream()
                .map(entity -> CommunityPostVO.builder()
                        .id(entity.getId())
                        .creatorId(entity.getCreatorId())
                        .authorName(entity.getAuthorName())
                        .forumName(entity.getForumName())
                        .title(entity.getTitle())
                        .summary(entity.getSummary())
                        .content(normalizePostContent(entity.getContent()))
                        .replyCount(entity.getReplyCount())
                        .likeCount(entity.getLikeCount())
                        .favoriteCount(entity.getFavoriteCount())
                        .status(entity.getStatus())
                        .liked(likedPostIds.contains(entity.getId()))
                        .favorited(favoritedPostIds.contains(entity.getId()))
                        .createdAt(entity.getCreatedAt())
                        .updatedAt(entity.getUpdatedAt())
                        .build())
                .toList();
    }

    private Set<Long> findActiveActionPostIds(Long currentUserId, List<Long> postIds, Integer actionType) {
        if (currentUserId == null || postIds.isEmpty()) {
            return Collections.emptySet();
        }
        return communityPostActionMapper.selectActiveByUserAndPostIds(currentUserId, actionType, postIds).stream()
                .map(CommunityPostActionEntity::getPostId)
                .collect(Collectors.toSet());
    }

    private CommunityReplyVO toReplyVO(CommunityReplyEntity entity) {
        return CommunityReplyVO.builder()
                .id(entity.getId())
                .postId(entity.getPostId())
                .creatorId(entity.getCreatorId())
                .parentReplyId(entity.getParentReplyId())
                .authorName(entity.getAuthorName())
                .replyToAuthorName(entity.getReplyToAuthorName())
                .content(entity.getContent())
                .likeCount(entity.getLikeCount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private void togglePostAction(Long userId, Long postId, int actionType) {
        CommunityPostActionEntity action = communityPostActionMapper.selectByPostIdAndUserIdAndType(postId, userId, actionType);
        boolean enableAction = action == null || !Objects.equals(action.getStatus(), STATUS_ENABLED);

        if (action == null) {
            CommunityPostActionEntity entity = new CommunityPostActionEntity();
            entity.setPostId(postId);
            entity.setUserId(userId);
            entity.setActionType(actionType);
            entity.setStatus(STATUS_ENABLED);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            communityPostActionMapper.insert(entity);
        } else {
            communityPostActionMapper.updateStatus(action.getId(), enableAction ? STATUS_ENABLED : STATUS_DISABLED);
        }

        if (actionType == ACTION_LIKE) {
            if (enableAction) {
                communityPostMapper.incrementLike(postId);
            } else {
                communityPostMapper.decrementLike(postId);
            }
            return;
        }

        if (enableAction) {
            communityPostMapper.incrementFavorite(postId);
        } else {
            communityPostMapper.decrementFavorite(postId);
        }
    }

    private void refreshReplyCount(Long postId) {
        int activeReplyCount = communityReplyMapper.countActiveByPostId(postId);
        communityPostMapper.updateReplyCount(postId, activeReplyCount);
    }

    private void notifyReplyParticipants(CommunityPostEntity post,
                                         CommunityReplyEntity parentReply,
                                         CommunityReplyEntity reply) {
        Set<Long> notifiedUserIds = new HashSet<>();
        if (parentReply != null) {
            String actionUrl = buildReplyNotificationActionUrl(post.getId(), parentReply.getId());
            notifyReplyRecipient(
                    parentReply.getCreatorId(),
                    reply.getCreatorId(),
                    "你的评论收到了新回复",
                    buildCommentReplyNotificationContent(post, reply),
                    actionUrl,
                    notifiedUserIds
            );
            notifyReplyRecipient(
                    post.getCreatorId(),
                    reply.getCreatorId(),
                    "你的帖子有了新的评论互动",
                    buildPostThreadReplyNotificationContent(post, parentReply, reply),
                    actionUrl,
                    notifiedUserIds
            );
            return;
        }

        notifyReplyRecipient(
                post.getCreatorId(),
                reply.getCreatorId(),
                "你的帖子收到了新回复",
                buildPostReplyNotificationContent(post, reply),
                buildReplyNotificationActionUrl(post.getId(), reply.getId()),
                notifiedUserIds
        );
    }

    private void notifyReplyRecipient(Long receiverId,
                                      Long actorId,
                                      String title,
                                      String content,
                                      String actionUrl,
                                      Set<Long> notifiedUserIds) {
        if (receiverId == null || Objects.equals(receiverId, actorId) || !notifiedUserIds.add(receiverId)) {
            return;
        }
        inboxMessageService.sendSystemMessage(receiverId, title, content, actionUrl);
    }

    private String buildPostReplyNotificationContent(CommunityPostEntity post, CommunityReplyEntity reply) {
        return "%s 回复了你的帖子《%s》：%s".formatted(
                defaultText(reply.getAuthorName(), "有人"),
                defaultText(post.getTitle(), "该帖子"),
                buildReplyPreview(reply.getContent())
        );
    }

    private String buildCommentReplyNotificationContent(CommunityPostEntity post, CommunityReplyEntity reply) {
        return "%s 在《%s》中回复了你的评论：%s".formatted(
                defaultText(reply.getAuthorName(), "有人"),
                defaultText(post.getTitle(), "该帖子"),
                buildReplyPreview(reply.getContent())
        );
    }

    private String buildPostThreadReplyNotificationContent(CommunityPostEntity post,
                                                           CommunityReplyEntity parentReply,
                                                           CommunityReplyEntity reply) {
        return "%s 在《%s》中回复了 %s 的评论：%s".formatted(
                defaultText(reply.getAuthorName(), "有人"),
                defaultText(post.getTitle(), "该帖子"),
                defaultText(parentReply.getAuthorName(), "楼主"),
                buildReplyPreview(reply.getContent())
        );
    }

    private String buildReplyNotificationActionUrl(Long postId, Long replyId) {
        if (replyId == null) {
            return "/community/posts/" + postId;
        }
        return "/community/posts/" + postId + "?replyId=" + replyId;
    }

    private String buildReplyPreview(String content) {
        String preview = trimToNull(content);
        if (preview == null) {
            return "";
        }
        preview = preview.replaceAll("\\s+", " ");
        if (preview.length() <= REPLY_NOTIFICATION_PREVIEW_MAX_LENGTH) {
            return preview;
        }
        return preview.substring(0, REPLY_NOTIFICATION_PREVIEW_MAX_LENGTH) + "...";
    }

    private String defaultText(String value, String fallback) {
        String normalized = trimToNull(value);
        return normalized == null ? fallback : normalized;
    }

    private CommunityPostEntity getActivePostOrThrow(Long postId) {
        CommunityPostEntity entity = getPostOrThrow(postId);
        if (!Objects.equals(entity.getStatus(), STATUS_ENABLED)) {
            throw new BizException(404, "帖子不存在或已下架");
        }
        return entity;
    }

    private CommunityPostEntity getPostOrThrow(Long postId) {
        CommunityPostEntity entity = communityPostMapper.selectById(postId);
        if (entity == null) {
            throw new BizException(404, "帖子不存在");
        }
        return entity;
    }

    private CommunityReplyEntity getReplyOrThrow(Long replyId) {
        CommunityReplyEntity entity = communityReplyMapper.selectById(replyId);
        if (entity == null) {
            throw new BizException(404, "回复不存在");
        }
        return entity;
    }

    private CommunityReplyEntity getActiveReplyOrThrow(Long replyId) {
        CommunityReplyEntity entity = getReplyOrThrow(replyId);
        if (!Objects.equals(entity.getStatus(), STATUS_ENABLED)) {
            throw new BizException(404, "回复不存在或已下架");
        }
        return entity;
    }

    private UserEntity getUserOrThrow(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    private String resolveAuthorName(UserEntity user) {
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname().trim();
        }
        if (StringUtils.hasText(user.getPhone())) {
            return user.getPhone().trim();
        }
        return user.getUserNo();
    }

    private String resolveSummary(String summary, String content) {
        String normalized = trimToNull(summary);
        if (normalized != null) {
            return normalized;
        }
        String plainContent = content.replaceAll("\\s+", " ").trim();
        if (plainContent.length() <= 120) {
            return plainContent;
        }
        return plainContent.substring(0, 120) + "...";
    }

    private CommunityReplyEntity resolveParentReply(Long postId, Long parentReplyId) {
        if (parentReplyId == null) {
            return null;
        }
        CommunityReplyEntity reply = getActiveReplyOrThrow(parentReplyId);
        if (!Objects.equals(reply.getPostId(), postId)) {
            throw new BizException(400, "不能回复其他帖子的评论");
        }
        return reply;
    }

    private String sanitizePostContent(String rawContent) {
        String normalized = trimToNull(rawContent);
        if (normalized == null) {
            throw new BizException(400, "正文不能为空");
        }
        String sanitized = sanitizeRichHtml(normalized);
        if (!StringUtils.hasText(sanitized)) {
            throw new BizException(400, "正文不能为空");
        }
        String plainText = extractPlainText(sanitized);
        int plainLength = plainText.length();
        if (plainLength < POST_CONTENT_MIN_LENGTH || plainLength > POST_CONTENT_MAX_LENGTH) {
            throw new BizException(400, "正文长度需在10到10000个字符之间");
        }
        return sanitized;
    }

    private String normalizePostContent(String storedContent) {
        String normalized = trimToNull(storedContent);
        if (normalized == null) {
            return "";
        }
        if (looksLikeHtml(normalized)) {
            return sanitizeRichHtml(normalized);
        }
        return convertPlainTextToHtml(normalized);
    }

    private boolean looksLikeHtml(String value) {
        return HTML_TAG_PATTERN.matcher(value).find();
    }

    private String extractPlainText(String content) {
        return Jsoup.parse(content).text().replace('\u00A0', ' ').trim();
    }

    private String sanitizeRichHtml(String content) {
        return Jsoup.clean(
                content,
                "",
                COMMUNITY_CONTENT_SAFELIST,
                new Document.OutputSettings().prettyPrint(false)
        ).trim();
    }

    private String convertPlainTextToHtml(String plainText) {
        return Arrays.stream(plainText.split("\\R{2,}"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(paragraph -> "<p>" + HtmlUtils.htmlEscape(paragraph).replace("\n", "<br/>").replace("\r", "") + "</p>")
                .collect(Collectors.joining());
    }

    private String normalizeForumName(String forumName) {
        String normalized = trimToNull(forumName);
        if (normalized == null || "全部".equals(normalized)) {
            return null;
        }
        return normalized;
    }

    private String normalizeKeyword(String keyword) {
        return trimToNull(keyword);
    }

    private Integer normalizeStatusFilter(Integer status) {
        if (status == null) {
            return null;
        }
        return normalizeRequiredStatus(status);
    }

    private int normalizeRequiredStatus(Integer status) {
        if (!Objects.equals(status, STATUS_ENABLED) && !Objects.equals(status, STATUS_DISABLED)) {
            throw new BizException(400, "状态不合法");
        }
        return status;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }
}
