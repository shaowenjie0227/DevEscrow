package com.programmer.escrow.chat.service;

import com.programmer.escrow.chat.dto.ChatSendDTO;
import com.programmer.escrow.chat.entity.ChatMessageEntity;
import com.programmer.escrow.chat.mapper.ChatMessageMapper;
import com.programmer.escrow.chat.vo.ChatConversationVO;
import com.programmer.escrow.chat.vo.ChatMessageVO;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.demand.entity.DemandEntity;
import com.programmer.escrow.demand.mapper.DemandMapper;
import com.programmer.escrow.order.entity.OrderEntity;
import com.programmer.escrow.order.mapper.OrderMapper;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import com.programmer.escrow.user.service.UserBanLifecycleService;
import com.programmer.escrow.websocket.ChatWebSocketSessionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ChatServiceImpl implements ChatService {

    private static final int BIZ_TYPE_DEMAND = 1;
    private static final int BIZ_TYPE_ORDER = 2;
    private static final int MSG_TYPE_TEXT = 1;
    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_RECALLED = 2;

    private final ChatMessageMapper chatMessageMapper;
    private final DemandMapper demandMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final UserBanLifecycleService userBanLifecycleService;
    private final ChatWebSocketSessionManager chatWebSocketSessionManager;

    public ChatServiceImpl(ChatMessageMapper chatMessageMapper,
                           DemandMapper demandMapper,
                           OrderMapper orderMapper,
                           UserMapper userMapper,
                           UserBanLifecycleService userBanLifecycleService,
                           ChatWebSocketSessionManager chatWebSocketSessionManager) {
        this.chatMessageMapper = chatMessageMapper;
        this.demandMapper = demandMapper;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.userBanLifecycleService = userBanLifecycleService;
        this.chatWebSocketSessionManager = chatWebSocketSessionManager;
    }

    @Override
    public List<ChatConversationVO> listMyConversations(Long userId) {
        UserEntity currentUser = requireActiveUser(userId);
        List<ChatMessageEntity> messages = chatMessageMapper.selectByUserId(userId);
        return buildConversationSummaries(messages, currentUser, false, null).stream()
                .sorted(Comparator.comparing(ChatConversationVO::getLastMessageAt).reversed())
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ChatMessageVO> listConversationMessages(Long userId,
                                                        Integer bizType,
                                                        Long demandId,
                                                        Long orderId,
                                                        Long partnerId) {
        ConversationContext context = resolveConversationContext(userId, partnerId, bizType, demandId, orderId, false);
        List<ChatMessageEntity> messages = chatMessageMapper.selectConversationMessages(
                context.bizType(),
                context.demandId(),
                context.orderId(),
                context.leftUserId(),
                context.rightUserId()
        );
        chatMessageMapper.markConversationRead(
                context.bizType(),
                context.demandId(),
                context.orderId(),
                userId,
                partnerId,
                LocalDateTime.now()
        );
        return buildMessageVOs(messages, userId, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageVO sendMessage(Long userId, ChatSendDTO dto) {
        ConversationContext context = resolveConversationContext(
                userId,
                dto.getReceiverId(),
                dto.getBizType(),
                dto.getDemandId(),
                dto.getOrderId(),
                true
        );

        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setBizType(context.bizType());
        entity.setDemandId(context.demandId());
        entity.setOrderId(context.orderId());
        entity.setSenderId(userId);
        entity.setReceiverId(dto.getReceiverId());
        entity.setMsgType(dto.getMsgType() == null ? MSG_TYPE_TEXT : dto.getMsgType());
        entity.setContent(dto.getContent().trim());
        entity.setFileUrl(dto.getFileUrl());
        entity.setReadStatus(0);
        entity.setStatus(STATUS_NORMAL);
        entity.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(entity);
        chatWebSocketSessionManager.notifyChatMessage(
                entity.getBizType(),
                entity.getDemandId(),
                entity.getOrderId(),
                entity.getSenderId(),
                entity.getReceiverId()
        );
        return toMessageVO(entity, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageVO recallMessage(Long userId, Long messageId) {
        ChatMessageEntity message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BizException(404, "message does not exist");
        }
        if (!Objects.equals(message.getSenderId(), userId)) {
            throw new BizException(403, "you can only recall your own message");
        }
        if (!Objects.equals(message.getStatus(), STATUS_NORMAL)) {
            throw new BizException(400, "message cannot be recalled");
        }
        int updated = chatMessageMapper.updateRecalled(messageId, userId, LocalDateTime.now());
        if (updated == 0) {
            throw new BizException(400, "message cannot be recalled");
        }
        ChatMessageEntity latest = chatMessageMapper.selectById(messageId);
        chatWebSocketSessionManager.notifyChatMessage(
                latest.getBizType(),
                latest.getDemandId(),
                latest.getOrderId(),
                latest.getSenderId(),
                latest.getReceiverId()
        );
        return toMessageVO(latest, userId);
    }

    @Override
    public List<ChatConversationVO> listAdminConversations(String keyword) {
        List<ChatMessageEntity> messages = chatMessageMapper.selectAll();
        return buildConversationSummaries(messages, null, true, keyword).stream()
                .sorted(Comparator.comparing(ChatConversationVO::getLastMessageAt).reversed())
                .toList();
    }

    @Override
    public List<ChatMessageVO> listAdminConversationMessages(Integer bizType,
                                                             Long demandId,
                                                             Long orderId,
                                                             Long clientId,
                                                             Long developerId) {
        if (clientId == null || developerId == null) {
            throw new BizException(400, "clientId and developerId are required");
        }
        ConversationContext context = resolveAdminConversationContext(bizType, demandId, orderId, clientId, developerId);
        List<ChatMessageEntity> messages = chatMessageMapper.selectConversationMessages(
                context.bizType(),
                context.demandId(),
                context.orderId(),
                context.leftUserId(),
                context.rightUserId()
        );
        return buildMessageVOs(messages, null, true);
    }

    private List<ChatConversationVO> buildConversationSummaries(List<ChatMessageEntity> messages,
                                                                UserEntity currentUser,
                                                                boolean adminMode,
                                                                String keyword) {
        Map<String, ConversationAccumulator> conversations = new LinkedHashMap<>();
        Map<Long, UserEntity> userCache = new HashMap<>();
        Map<Long, DemandEntity> demandCache = new HashMap<>();
        Map<Long, OrderEntity> orderCache = new HashMap<>();

        for (ChatMessageEntity message : messages) {
            ConversationDescriptor descriptor = describeConversation(
                    message,
                    currentUser == null ? null : currentUser.getId(),
                    adminMode,
                    userCache,
                    demandCache,
                    orderCache
            );
            if (descriptor == null) {
                continue;
            }
            ConversationAccumulator accumulator = conversations.computeIfAbsent(
                    descriptor.key(),
                    ignored -> new ConversationAccumulator(descriptor.summary())
            );
            accumulator.incrementMessageCount();
            if (currentUser != null
                    && Objects.equals(message.getReceiverId(), currentUser.getId())
                    && Objects.equals(message.getReadStatus(), 0)) {
                accumulator.incrementUnread();
            }
        }

        return conversations.values().stream()
                .map(ConversationAccumulator::toVO)
                .filter(conversation -> matchesKeyword(conversation, keyword))
                .toList();
    }

    private ConversationDescriptor describeConversation(ChatMessageEntity message,
                                                        Long currentUserId,
                                                        boolean adminMode,
                                                        Map<Long, UserEntity> userCache,
                                                        Map<Long, DemandEntity> demandCache,
                                                        Map<Long, OrderEntity> orderCache) {
        if (!Objects.equals(message.getStatus(), STATUS_NORMAL) && !Objects.equals(message.getStatus(), STATUS_RECALLED)) {
            return null;
        }

        UserEntity sender = loadUser(userCache, message.getSenderId());
        UserEntity receiver = loadUser(userCache, message.getReceiverId());
        if (sender == null || receiver == null) {
            return null;
        }

        DemandEntity demand = message.getDemandId() == null ? null : loadDemand(demandCache, message.getDemandId());
        OrderEntity order = message.getOrderId() == null ? null : loadOrder(orderCache, message.getOrderId());

        Long clientId = null;
        String clientNickname = null;
        Long developerId = null;
        String developerNickname = null;

        if (Objects.equals(message.getBizType(), BIZ_TYPE_DEMAND) && demand != null) {
            clientId = demand.getPublisherId();
            UserEntity client = loadUser(userCache, clientId);
            clientNickname = client == null ? null : client.getNickname();
            Long anotherUserId = Objects.equals(message.getSenderId(), clientId) ? message.getReceiverId() : message.getSenderId();
            UserEntity developer = loadUser(userCache, anotherUserId);
            developerId = developer == null ? null : developer.getId();
            developerNickname = developer == null ? null : developer.getNickname();
        } else if (Objects.equals(message.getBizType(), BIZ_TYPE_ORDER) && order != null) {
            clientId = order.getClientId();
            developerId = order.getDeveloperId();
            UserEntity client = loadUser(userCache, clientId);
            UserEntity developer = loadUser(userCache, developerId);
            clientNickname = client == null ? null : client.getNickname();
            developerNickname = developer == null ? null : developer.getNickname();
        }

        Long partnerId = null;
        String partnerNickname = null;
        String partnerAvatarUrl = null;
        if (currentUserId != null) {
            UserEntity partner = Objects.equals(message.getSenderId(), currentUserId) ? receiver : sender;
            partnerId = partner.getId();
            partnerNickname = partner.getNickname();
            partnerAvatarUrl = partner.getAvatarUrl();
        }

        String demandNo = demand == null ? null : demand.getDemandNo();
        String demandTitle = demand == null
                ? (order == null ? null : order.getOrderTitle())
                : demand.getTitle();

        String preview = previewMessage(message);
        String key = buildConversationKey(
                message.getBizType(),
                message.getDemandId(),
                message.getOrderId(),
                message.getSenderId(),
                message.getReceiverId()
        );

        ChatConversationVO summary = ChatConversationVO.builder()
                .bizType(message.getBizType())
                .demandId(message.getDemandId())
                .orderId(message.getOrderId())
                .demandNo(demandNo)
                .demandTitle(demandTitle)
                .partnerId(partnerId)
                .partnerNickname(partnerNickname)
                .partnerAvatarUrl(partnerAvatarUrl)
                .clientId(clientId)
                .clientNickname(clientNickname)
                .developerId(developerId)
                .developerNickname(developerNickname)
                .lastMessage(preview)
                .lastMessageType(message.getMsgType())
                .lastMessageAt(message.getCreatedAt())
                .unreadCount(0)
                .messageCount(0)
                .build();
        return new ConversationDescriptor(key, summary);
    }

    private boolean matchesKeyword(ChatConversationVO conversation, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalized = keyword.trim().toLowerCase();
        List<String> parts = new ArrayList<>();
        parts.add(conversation.getDemandNo());
        parts.add(conversation.getDemandTitle());
        parts.add(conversation.getPartnerNickname());
        parts.add(conversation.getClientNickname());
        parts.add(conversation.getDeveloperNickname());
        parts.add(conversation.getLastMessage());
        return parts.stream()
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .anyMatch(value -> value.contains(normalized));
    }

    private List<ChatMessageVO> buildMessageVOs(List<ChatMessageEntity> messages, Long currentUserId, boolean adminView) {
        Map<Long, UserEntity> userCache = new HashMap<>();
        return messages.stream()
                .map(message -> {
                    UserEntity sender = loadUser(userCache, message.getSenderId());
                    UserEntity receiver = loadUser(userCache, message.getReceiverId());
                    String content = resolveMessageContent(message, adminView);
                    String fileUrl = resolveMessageFileUrl(message, adminView);
                    return ChatMessageVO.builder()
                            .id(message.getId())
                            .bizType(message.getBizType())
                            .demandId(message.getDemandId())
                            .orderId(message.getOrderId())
                            .senderId(message.getSenderId())
                            .senderNickname(sender == null ? null : sender.getNickname())
                            .senderAvatarUrl(sender == null ? null : sender.getAvatarUrl())
                            .receiverId(message.getReceiverId())
                            .receiverNickname(receiver == null ? null : receiver.getNickname())
                            .receiverAvatarUrl(receiver == null ? null : receiver.getAvatarUrl())
                            .msgType(message.getMsgType())
                            .content(content)
                            .fileUrl(fileUrl)
                            .status(message.getStatus())
                            .readStatus(message.getReadStatus())
                            .readAt(message.getReadAt())
                            .createdAt(message.getCreatedAt())
                            .self(currentUserId == null ? null : Objects.equals(message.getSenderId(), currentUserId))
                            .build();
                })
                .toList();
    }

    private ChatMessageVO toMessageVO(ChatMessageEntity entity, Long currentUserId) {
        Map<Long, UserEntity> cache = new HashMap<>();
        UserEntity sender = loadUser(cache, entity.getSenderId());
        UserEntity receiver = loadUser(cache, entity.getReceiverId());
        String content = resolveMessageContent(entity, false);
        String fileUrl = resolveMessageFileUrl(entity, false);
        return ChatMessageVO.builder()
                .id(entity.getId())
                .bizType(entity.getBizType())
                .demandId(entity.getDemandId())
                .orderId(entity.getOrderId())
                .senderId(entity.getSenderId())
                .senderNickname(sender == null ? null : sender.getNickname())
                .senderAvatarUrl(sender == null ? null : sender.getAvatarUrl())
                .receiverId(entity.getReceiverId())
                .receiverNickname(receiver == null ? null : receiver.getNickname())
                .receiverAvatarUrl(receiver == null ? null : receiver.getAvatarUrl())
                .msgType(entity.getMsgType())
                .content(content)
                .fileUrl(fileUrl)
                .status(entity.getStatus())
                .readStatus(entity.getReadStatus())
                .readAt(entity.getReadAt())
                .createdAt(entity.getCreatedAt())
                .self(currentUserId == null ? null : Objects.equals(entity.getSenderId(), currentUserId))
                .build();
    }

    private ConversationContext resolveConversationContext(Long currentUserId,
                                                          Long partnerId,
                                                          Integer bizType,
                                                          Long demandId,
                                                          Long orderId,
                                                          boolean sending) {
        if (partnerId == null) {
            throw new BizException(400, "partnerId is required");
        }
        if (Objects.equals(currentUserId, partnerId)) {
            throw new BizException(400, "cannot chat with yourself");
        }
        requireActiveUser(currentUserId);
        requireActiveUser(partnerId);

        if (Objects.equals(bizType, BIZ_TYPE_DEMAND)) {
            return resolveDemandConversation(currentUserId, partnerId, demandId);
        }
        if (Objects.equals(bizType, BIZ_TYPE_ORDER)) {
            return resolveOrderConversation(currentUserId, partnerId, orderId);
        }
        throw new BizException(400, sending ? "unsupported chat type" : "invalid conversation type");
    }

    private ConversationContext resolveAdminConversationContext(Integer bizType,
                                                               Long demandId,
                                                               Long orderId,
                                                               Long clientId,
                                                               Long developerId) {
        requireActiveUser(clientId);
        requireActiveUser(developerId);
        if (Objects.equals(bizType, BIZ_TYPE_DEMAND)) {
            if (demandId == null) {
                throw new BizException(400, "demandId is required");
            }
            DemandEntity demand = demandMapper.selectById(demandId);
            if (demand == null) {
                throw new BizException(404, "demand does not exist");
            }
            return new ConversationContext(BIZ_TYPE_DEMAND, demandId, null, clientId, developerId);
        }
        if (Objects.equals(bizType, BIZ_TYPE_ORDER)) {
            if (orderId == null) {
                throw new BizException(400, "orderId is required");
            }
            OrderEntity order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new BizException(404, "order does not exist");
            }
            return new ConversationContext(BIZ_TYPE_ORDER, order.getDemandId(), orderId, clientId, developerId);
        }
        throw new BizException(400, "unsupported chat type");
    }

    private ConversationContext resolveDemandConversation(Long currentUserId, Long partnerId, Long demandId) {
        if (demandId == null) {
            throw new BizException(400, "demandId is required");
        }
        DemandEntity demand = demandMapper.selectById(demandId);
        if (demand == null) {
            throw new BizException(404, "demand does not exist");
        }
        UserEntity currentUser = requireActiveUser(currentUserId);
        UserEntity partner = requireActiveUser(partnerId);

        boolean currentIsPublisher = Objects.equals(demand.getPublisherId(), currentUserId);
        boolean partnerIsPublisher = Objects.equals(demand.getPublisherId(), partnerId);
        if (!currentIsPublisher && !partnerIsPublisher) {
            throw new BizException(403, "one participant must be the demand publisher");
        }

        UserEntity developer = currentIsPublisher ? partner : currentUser;
        if (!isApprovedDeveloper(developer)) {
            throw new BizException(403, "developer verification is required before chatting");
        }

        return new ConversationContext(BIZ_TYPE_DEMAND, demandId, null, currentUserId, partnerId);
    }

    private ConversationContext resolveOrderConversation(Long currentUserId, Long partnerId, Long orderId) {
        if (orderId == null) {
            throw new BizException(400, "orderId is required");
        }
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(404, "order does not exist");
        }
        boolean validPair = (Objects.equals(order.getClientId(), currentUserId) && Objects.equals(order.getDeveloperId(), partnerId))
                || (Objects.equals(order.getDeveloperId(), currentUserId) && Objects.equals(order.getClientId(), partnerId));
        if (!validPair) {
            throw new BizException(403, "you are not allowed to view this order conversation");
        }
        return new ConversationContext(BIZ_TYPE_ORDER, order.getDemandId(), orderId, currentUserId, partnerId);
    }

    private UserEntity requireActiveUser(Long userId) {
        UserEntity user = userBanLifecycleService.normalizeBanStatus(userMapper.selectById(userId));
        if (user == null || !Objects.equals(user.getStatus(), 1)) {
            throw new BizException(404, "user does not exist");
        }
        return user;
    }

    private boolean isApprovedDeveloper(UserEntity user) {
        return user != null
                && Objects.equals(user.getStatus(), 1)
                && Objects.equals(user.getDeveloperStatus(), 2);
    }

    private UserEntity loadUser(Map<Long, UserEntity> cache, Long userId) {
        if (userId == null) {
            return null;
        }
        return cache.computeIfAbsent(userId, userMapper::selectById);
    }

    private DemandEntity loadDemand(Map<Long, DemandEntity> cache, Long demandId) {
        if (demandId == null) {
            return null;
        }
        return cache.computeIfAbsent(demandId, demandMapper::selectById);
    }

    private OrderEntity loadOrder(Map<Long, OrderEntity> cache, Long orderId) {
        if (orderId == null) {
            return null;
        }
        return cache.computeIfAbsent(orderId, orderMapper::selectById);
    }

    private String previewMessage(ChatMessageEntity message) {
        if (Objects.equals(message.getStatus(), STATUS_RECALLED)) {
            return "[message recalled]";
        }
        if (Objects.equals(message.getMsgType(), 2)) {
            return "[image]";
        }
        if (Objects.equals(message.getMsgType(), 3)) {
            return "[file]";
        }
        if (Objects.equals(message.getMsgType(), 4)) {
            return "[system]";
        }
        return message.getContent();
    }

    private String resolveMessageContent(ChatMessageEntity message, boolean adminView) {
        if (adminView || !Objects.equals(message.getStatus(), STATUS_RECALLED)) {
            return message.getContent();
        }
        return "message recalled";
    }

    private String resolveMessageFileUrl(ChatMessageEntity message, boolean adminView) {
        if (adminView || !Objects.equals(message.getStatus(), STATUS_RECALLED)) {
            return message.getFileUrl();
        }
        return null;
    }

    private String buildConversationKey(Integer bizType,
                                        Long demandId,
                                        Long orderId,
                                        Long leftUserId,
                                        Long rightUserId) {
        long minUserId = Math.min(leftUserId == null ? 0L : leftUserId, rightUserId == null ? 0L : rightUserId);
        long maxUserId = Math.max(leftUserId == null ? 0L : leftUserId, rightUserId == null ? 0L : rightUserId);
        return bizType + ":" + (demandId == null ? 0L : demandId) + ":" + (orderId == null ? 0L : orderId) + ":" + minUserId + ":" + maxUserId;
    }

    private record ConversationContext(Integer bizType,
                                       Long demandId,
                                       Long orderId,
                                       Long leftUserId,
                                       Long rightUserId) {
    }

    private record ConversationDescriptor(String key, ChatConversationVO summary) {
    }

    private static final class ConversationAccumulator {
        private final ChatConversationVO summary;
        private int unreadCount;
        private int messageCount;

        private ConversationAccumulator(ChatConversationVO summary) {
            this.summary = summary;
        }

        private void incrementUnread() {
            unreadCount++;
        }

        private void incrementMessageCount() {
            messageCount++;
        }

        private ChatConversationVO toVO() {
            summary.setUnreadCount(unreadCount);
            summary.setMessageCount(messageCount);
            return summary;
        }
    }
}
