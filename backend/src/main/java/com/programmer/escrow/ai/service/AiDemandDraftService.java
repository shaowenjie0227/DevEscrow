package com.programmer.escrow.ai.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.programmer.escrow.ai.dto.AiDemandDraftGenerateDTO;
import com.programmer.escrow.ai.model.AiCallLogCreateCommand;
import com.programmer.escrow.ai.model.AiContextDocument;
import com.programmer.escrow.ai.model.AiPromptRuntimeConfig;
import com.programmer.escrow.ai.model.AiRuntimeConfig;
import com.programmer.escrow.ai.vo.AiDemandDraftStageVO;
import com.programmer.escrow.ai.vo.AiDemandDraftVO;
import com.programmer.escrow.ai.vo.AiDemandReferenceVO;
import com.programmer.escrow.auth.context.UserContextHolder;
import com.programmer.escrow.common.constant.RedisKeys;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.demand.entity.DemandCategoryEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AiDemandDraftService {

    private static final Logger log = LoggerFactory.getLogger(AiDemandDraftService.class);
    private static final long STREAM_TIMEOUT_MS = 120_000L;
    private static final int[] STREAM_PROGRESS_THRESHOLDS = {1, 120, 280, 520};
    private static final int[] STREAM_PROGRESS_VALUES = {36, 54, 72, 88};
    private static final String[] STREAM_PROGRESS_STAGES = {
            "AI 已开始起草",
            "预算与工期推演中",
            "阶段拆分整理中",
            "正在收束最终结构"
    };
    private static final String[] STREAM_PROGRESS_MESSAGES = {
            "已经收到模型返回内容，先帮你起草标题和摘要。",
            "我正在把复杂度、周期和预算区间压成更适合报价的范围。",
            "我继续把需求拆成更适合验收和托管的交付阶段。",
            "最后一步，我在校正分类、风险提示和最终发布结构。"
    };

    private final AiContextRetrievalService aiContextRetrievalService;
    private final AiPromptManager aiPromptManager;
    private final AiModelClient aiModelClient;
    private final AiDemandDraftFallbackService aiDemandDraftFallbackService;
    private final AiCallLogService aiCallLogService;
    private final AiRuntimeConfigService aiRuntimeConfigService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public AiDemandDraftService(AiContextRetrievalService aiContextRetrievalService,
                                AiPromptManager aiPromptManager,
                                AiModelClient aiModelClient,
                                AiDemandDraftFallbackService aiDemandDraftFallbackService,
                                AiCallLogService aiCallLogService,
                                AiRuntimeConfigService aiRuntimeConfigService,
                                StringRedisTemplate stringRedisTemplate,
                                ObjectMapper objectMapper) {
        this.aiContextRetrievalService = aiContextRetrievalService;
        this.aiPromptManager = aiPromptManager;
        this.aiModelClient = aiModelClient;
        this.aiDemandDraftFallbackService = aiDemandDraftFallbackService;
        this.aiCallLogService = aiCallLogService;
        this.aiRuntimeConfigService = aiRuntimeConfigService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public AiDemandDraftVO generateDraft(AiDemandDraftGenerateDTO request) {
        long startAt = System.currentTimeMillis();
        String requirement = normalizeRequirement(request.getRequirement());
        if (requirement.length() < 8) {
            throw new BizException(400, "需求描述太短了，至少再补充一点业务目标或功能范围");
        }

        AiRuntimeConfig runtimeConfig = aiRuntimeConfigService.resolveEffectiveConfig();
        AiPromptRuntimeConfig promptConfig = aiPromptManager.resolveDemandDraftConfig(runtimeConfig);
        String requestDigest = DigestUtil.sha256Hex(buildCacheFingerprint(request, promptConfig));
        String cacheKey = String.format(RedisKeys.AI_DEMAND_DRAFT, requestDigest);
        Long creatorUserId = currentUserId();

        boolean cacheHit = false;
        boolean fallbackUsed = true;
        int logStatus = 1;
        int contextCount = 0;
        String errorMessage = null;
        String resultPreview = null;

        try {
            AiDemandDraftVO cachedDraft = readCache(cacheKey);
            if (cachedDraft != null) {
                cacheHit = true;
                fallbackUsed = Boolean.TRUE.equals(cachedDraft.getFallbackUsed());
                resultPreview = buildResultPreview(cachedDraft);
                cachedDraft.setFromCache(true);
                return cachedDraft;
            }

            List<DemandCategoryEntity> categories = aiContextRetrievalService.listActiveCategories();
            List<AiContextDocument> references = aiContextRetrievalService.retrieve(requirement, resolveTopK(promptConfig));
            contextCount = references.size();

            AiDemandDraftVO fallbackDraft = aiDemandDraftFallbackService.buildDraft(
                    request,
                    categories,
                    references,
                    promptConfig.getPromptVersion()
            );

            AiDemandDraftVO finalDraft = fallbackDraft;
            if (runtimeConfig.isEnabled() && StringUtils.hasText(runtimeConfig.getApiKey())) {
                try {
                    String content = aiModelClient.chat(
                            aiPromptManager.buildSystemPrompt(promptConfig),
                            aiPromptManager.buildUserPrompt(promptConfig, request, categories, references),
                            promptConfig.getModel(),
                            promptConfig.getTemperature(),
                            runtimeConfig
                    );
                    finalDraft = normalizeDraft(content, request, categories, references, fallbackDraft);
                    if (finalDraft != fallbackDraft) {
                        finalDraft.setFallbackUsed(false);
                    }
                } catch (Exception ex) {
                    if (!runtimeConfig.isFallbackEnabled()) {
                        throw new BizException(500, "AI 生成失败：" + ex.getMessage());
                    }
                    log.warn("AI draft generation failed, fallback will be used: {}", ex.getMessage());
                }
            }

            finalDraft.setPromptVersion(promptConfig.getPromptVersion());
            finalDraft.setReferences(toReferences(references));
            finalDraft.setFromCache(false);
            fallbackUsed = Boolean.TRUE.equals(finalDraft.getFallbackUsed());
            resultPreview = buildResultPreview(finalDraft);
            writeCache(cacheKey, finalDraft, runtimeConfig);
            return finalDraft;
        } catch (BizException ex) {
            logStatus = 2;
            errorMessage = ex.getMessage();
            throw ex;
        } catch (Exception ex) {
            logStatus = 2;
            errorMessage = ex.getMessage();
            throw ex;
        } finally {
            aiCallLogService.create(AiCallLogCreateCommand.builder()
                    .sceneCode(promptConfig.getSceneCode())
                    .promptVersion(promptConfig.getPromptVersion())
                    .provider(runtimeConfig.getProvider())
                    .model(promptConfig.getModel())
                    .creatorUserId(creatorUserId)
                    .requestDigest(requestDigest)
                    .requestPreview(requirement)
                    .contextCount(contextCount)
                    .cacheHit(cacheHit)
                    .fallbackUsed(fallbackUsed)
                    .status(logStatus)
                    .latencyMs(System.currentTimeMillis() - startAt)
                    .errorMessage(errorMessage)
                    .resultPreview(resultPreview)
                    .build());
        }
    }

    public SseEmitter streamDraft(AiDemandDraftGenerateDTO request) {
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);
        Long creatorUserId = currentUserId();
        AtomicBoolean streamOpen = new AtomicBoolean(true);
        emitter.onCompletion(() -> streamOpen.set(false));
        emitter.onTimeout(() -> streamOpen.set(false));
        emitter.onError(error -> streamOpen.set(false));
        CompletableFuture.runAsync(() -> doStreamDraft(request, emitter, creatorUserId, streamOpen));
        return emitter;
    }

    private void doStreamDraft(AiDemandDraftGenerateDTO request,
                               SseEmitter emitter,
                               Long creatorUserId,
                               AtomicBoolean streamOpen) {
        long startAt = System.currentTimeMillis();
        String requirement = normalizeRequirement(request.getRequirement());
        AiRuntimeConfig runtimeConfig = null;
        AiPromptRuntimeConfig promptConfig = null;
        String requestDigest = null;

        boolean cacheHit = false;
        boolean fallbackUsed = true;
        int logStatus = 1;
        int contextCount = 0;
        String errorMessage = null;
        String resultPreview = null;

        try {
            if (requirement.length() < 8) {
                throw new BizException(400, "需求描述太短了，至少再补充一点业务目标或功能范围");
            }

            runtimeConfig = aiRuntimeConfigService.resolveEffectiveConfig();
            promptConfig = aiPromptManager.resolveDemandDraftConfig(runtimeConfig);
            requestDigest = DigestUtil.sha256Hex(buildCacheFingerprint(request, promptConfig));
            String cacheKey = String.format(RedisKeys.AI_DEMAND_DRAFT, requestDigest);

            sendStatusEvent(emitter, streamOpen, "准备上下文", "我先读取你的需求描述、已有草稿和平台上下文。", 8);

            AiDemandDraftVO cachedDraft = readCache(cacheKey);
            if (cachedDraft != null) {
                cacheHit = true;
                fallbackUsed = Boolean.TRUE.equals(cachedDraft.getFallbackUsed());
                resultPreview = buildResultPreview(cachedDraft);
                cachedDraft.setFromCache(true);
                sendStatusEvent(emitter, streamOpen, "命中缓存", "这条需求最近整理过，我直接把缓存草稿回传给你。", 100);
                sendFinalEvent(emitter, streamOpen, cachedDraft);
                return;
            }

            List<DemandCategoryEntity> categories = aiContextRetrievalService.listActiveCategories();
            List<AiContextDocument> references = aiContextRetrievalService.retrieve(requirement, resolveTopK(promptConfig));
            contextCount = references.size();
            sendStatusEvent(emitter, streamOpen, "参考信息已就绪", "我已经拿到了需求分类、知识库和资源上下文。", 18);

            AiDemandDraftVO fallbackDraft = aiDemandDraftFallbackService.buildDraft(
                    request,
                    categories,
                    references,
                    promptConfig.getPromptVersion()
            );

            AiDemandDraftVO finalDraft = fallbackDraft;
            if (runtimeConfig.isEnabled() && StringUtils.hasText(runtimeConfig.getApiKey())) {
                sendStatusEvent(emitter, streamOpen, "连接 AI 模型", "我已经连上模型，开始起草结构化需求。", 28);
                try {
                    AtomicInteger milestoneIndex = new AtomicInteger(0);
                    AtomicInteger streamChars = new AtomicInteger(0);
                    String content = aiModelClient.chatStream(
                            aiPromptManager.buildSystemPrompt(promptConfig),
                            aiPromptManager.buildUserPrompt(promptConfig, request, categories, references),
                            promptConfig.getModel(),
                            promptConfig.getTemperature(),
                            runtimeConfig,
                            chunk -> maybeEmitStreamProgress(emitter, streamOpen, milestoneIndex, streamChars.addAndGet(chunk.length()))
                    );
                    finalDraft = normalizeDraft(content, request, categories, references, fallbackDraft);
                    if (finalDraft != fallbackDraft) {
                        finalDraft.setFallbackUsed(false);
                    }
                } catch (Exception ex) {
                    if (!runtimeConfig.isFallbackEnabled()) {
                        throw new BizException(500, "AI 生成失败：" + ex.getMessage());
                    }
                    log.warn("AI draft stream generation failed, fallback will be used: {}", ex.getMessage());
                    sendStatusEvent(emitter, streamOpen, "切换规则草稿", "流式生成不稳定，我先按平台规则给你整理一版可继续编辑的草稿。", 82);
                }
            } else {
                sendStatusEvent(emitter, streamOpen, "使用规则草稿", "当前没有启用在线模型，我先用平台规则整理一版结构化草稿。", 42);
            }

            finalDraft.setPromptVersion(promptConfig.getPromptVersion());
            finalDraft.setReferences(toReferences(references));
            finalDraft.setFromCache(false);
            fallbackUsed = Boolean.TRUE.equals(finalDraft.getFallbackUsed());
            resultPreview = buildResultPreview(finalDraft);
            writeCache(cacheKey, finalDraft, runtimeConfig);

            sendStatusEvent(
                    emitter,
                    streamOpen,
                    fallbackUsed ? "规则草稿已完成" : "结构化草稿已完成",
                    fallbackUsed ? "已经整理出一版规则草稿，你可以直接带入正式发布页继续补充。"
                            : "AI 已完成结构化整理，正在把最终草稿回传给你。",
                    100
            );
            sendFinalEvent(emitter, streamOpen, finalDraft);
        } catch (BizException ex) {
            logStatus = 2;
            errorMessage = ex.getMessage();
            sendErrorEvent(emitter, streamOpen, ex.getMessage());
        } catch (Exception ex) {
            logStatus = 2;
            errorMessage = ex.getMessage();
            log.warn("AI draft stream failed: {}", ex.getMessage(), ex);
            sendErrorEvent(emitter, streamOpen, StringUtils.hasText(ex.getMessage()) ? ex.getMessage() : "AI 流式生成失败");
        } finally {
            if (promptConfig != null && runtimeConfig != null && StringUtils.hasText(requestDigest)) {
                aiCallLogService.create(AiCallLogCreateCommand.builder()
                        .sceneCode(promptConfig.getSceneCode())
                        .promptVersion(promptConfig.getPromptVersion())
                        .provider(runtimeConfig.getProvider())
                        .model(promptConfig.getModel())
                        .creatorUserId(creatorUserId)
                        .requestDigest(requestDigest)
                        .requestPreview(requirement)
                        .contextCount(contextCount)
                        .cacheHit(cacheHit)
                        .fallbackUsed(fallbackUsed)
                        .status(logStatus)
                        .latencyMs(System.currentTimeMillis() - startAt)
                        .errorMessage(errorMessage)
                        .resultPreview(resultPreview)
                        .build());
            }
            completeEmitter(emitter, streamOpen);
        }
    }

    private AiDemandDraftVO normalizeDraft(String content,
                                           AiDemandDraftGenerateDTO request,
                                           List<DemandCategoryEntity> categories,
                                           List<AiContextDocument> references,
                                           AiDemandDraftVO fallbackDraft) {
        try {
            JsonNode root = objectMapper.readTree(extractJson(content));
            DemandCategoryEntity matchedCategory = resolveCategory(root, categories, request.getRequirement(), references, fallbackDraft);
            BigDecimal budgetMin = firstPositive(root.path("budgetMin"), fallbackDraft.getBudgetMin());
            BigDecimal budgetMax = firstPositive(root.path("budgetMax"), fallbackDraft.getBudgetMax());
            if (budgetMax.compareTo(budgetMin) < 0) {
                budgetMax = budgetMin.add(BigDecimal.valueOf(1000L)).setScale(2, RoundingMode.HALF_UP);
            }

            Integer deliveryType = root.path("deliveryType").asInt(fallbackDraft.getDeliveryType());
            if (!Objects.equals(deliveryType, 2)) {
                deliveryType = 1;
            }
            Integer orderType = root.path("orderType").asInt(fallbackDraft.getOrderType());
            if (!Objects.equals(orderType, 2)) {
                orderType = 1;
            }

            boolean urgent = root.path("urgent").asBoolean(Boolean.TRUE.equals(fallbackDraft.getUrgent()));
            BigDecimal urgentBonus = urgent
                    ? firstPositive(root.path("urgentBonus"), fallbackDraft.getUrgentBonus())
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            List<AiDemandDraftStageVO> normalizedStages = deliveryType == 2
                    ? normalizeStagePlans(root.path("stagePlans"), budgetMin, budgetMax, fallbackDraft.getStagePlans())
                    : List.of();

            AiDemandDraftVO result = AiDemandDraftVO.builder()
                    .title(firstText(root.path("title"), fallbackDraft.getTitle()))
                    .summary(firstText(root.path("summary"), fallbackDraft.getSummary()))
                    .detail(firstText(root.path("detail"), fallbackDraft.getDetail()))
                    .categoryId(matchedCategory == null ? fallbackDraft.getCategoryId() : matchedCategory.getId())
                    .categoryName(matchedCategory == null ? fallbackDraft.getCategoryName() : matchedCategory.getCategoryName())
                    .orderType(orderType)
                    .urgent(urgent)
                    .urgentBonus(urgentBonus.setScale(2, RoundingMode.HALF_UP))
                    .budgetMin(budgetMin.setScale(2, RoundingMode.HALF_UP))
                    .budgetMax(budgetMax.setScale(2, RoundingMode.HALF_UP))
                    .expectedDays(Math.max(1, root.path("expectedDays").asInt(fallbackDraft.getExpectedDays())))
                    .deliveryType(deliveryType)
                    .stagePlans(normalizedStages)
                    .recommendedSkills(normalizeStringList(root.path("recommendedSkills"), fallbackDraft.getRecommendedSkills()))
                    .riskTips(normalizeStringList(root.path("riskTips"), fallbackDraft.getRiskTips()))
                    .references(List.of())
                    .fromCache(false)
                    .fallbackUsed(false)
                    .promptVersion(fallbackDraft.getPromptVersion())
                    .build();

            if (Objects.equals(result.getDeliveryType(), 2) && CollectionUtils.isEmpty(result.getStagePlans())) {
                result.setStagePlans(fallbackDraft.getStagePlans());
            }
            return result;
        } catch (JsonProcessingException ex) {
            log.warn("Failed to parse AI JSON content, fallback will be used: {}", ex.getMessage());
            return fallbackDraft;
        }
    }

    private List<AiDemandDraftStageVO> normalizeStagePlans(JsonNode stagesNode,
                                                           BigDecimal budgetMin,
                                                           BigDecimal budgetMax,
                                                           List<AiDemandDraftStageVO> fallbackStages) {
        if (!stagesNode.isArray() || stagesNode.isEmpty()) {
            return fallbackStages;
        }
        List<AiDemandDraftStageVO> stages = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (int index = 0; index < stagesNode.size(); index++) {
            JsonNode node = stagesNode.get(index);
            BigDecimal amount = firstPositive(node.path("stageAmount"), BigDecimal.ZERO);
            String stageDesc = firstText(node.path("stageDesc"), null);
            if (amount.compareTo(BigDecimal.ZERO) <= 0 || !StringUtils.hasText(stageDesc)) {
                return fallbackStages;
            }
            total = total.add(amount);
            stages.add(AiDemandDraftStageVO.builder()
                    .stageNo(index + 1)
                    .stageName(firstText(node.path("stageName"), "第" + (index + 1) + "阶段"))
                    .stageDesc(stageDesc)
                    .stageAmount(amount.setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        if (total.compareTo(budgetMin) < 0 || total.compareTo(budgetMax) > 0) {
            return fallbackStages;
        }
        return stages;
    }

    private DemandCategoryEntity resolveCategory(JsonNode root,
                                                 List<DemandCategoryEntity> categories,
                                                 String requirement,
                                                 List<AiContextDocument> references,
                                                 AiDemandDraftVO fallbackDraft) {
        long categoryId = root.path("categoryId").asLong(0L);
        if (categoryId > 0) {
            for (DemandCategoryEntity category : categories) {
                if (Objects.equals(category.getId(), categoryId)) {
                    return category;
                }
            }
        }

        String categoryName = firstText(root.path("categoryName"), null);
        if (StringUtils.hasText(categoryName)) {
            for (DemandCategoryEntity category : categories) {
                if (categoryName.trim().equalsIgnoreCase(category.getCategoryName())) {
                    return category;
                }
            }
            for (DemandCategoryEntity category : categories) {
                String currentName = category.getCategoryName() == null ? "" : category.getCategoryName().toLowerCase(Locale.ROOT);
                String expectedName = categoryName.toLowerCase(Locale.ROOT);
                if (expectedName.contains(currentName) || currentName.contains(expectedName)) {
                    return category;
                }
            }
        }

        AiDemandDraftVO resolvedFallback = aiDemandDraftFallbackService.buildDraft(
                AiDemandDraftGenerateDTOBuilder.copyOf(requirement, root, fallbackDraft),
                categories,
                references,
                fallbackDraft.getPromptVersion()
        );
        if (resolvedFallback.getCategoryId() == null) {
            return categories.isEmpty() ? null : categories.get(0);
        }
        return categories.stream()
                .filter(category -> Objects.equals(category.getId(), resolvedFallback.getCategoryId()))
                .findFirst()
                .orElse(categories.isEmpty() ? null : categories.get(0));
    }

    private List<String> normalizeStringList(JsonNode node, List<String> fallback) {
        if (!node.isArray() || node.isEmpty()) {
            return fallback;
        }
        List<String> values = new ArrayList<>();
        node.forEach(item -> {
            String text = item.asText("").trim();
            if (!text.isEmpty()) {
                values.add(text);
            }
        });
        return values.isEmpty() ? fallback : values.stream().distinct().limit(6).toList();
    }

    private String firstText(JsonNode node, String fallback) {
        String text = node.asText(null);
        return StringUtils.hasText(text) ? text.trim() : fallback;
    }

    private BigDecimal firstPositive(JsonNode node, BigDecimal fallback) {
        String raw = node.asText(null);
        if (!StringUtils.hasText(raw)) {
            return fallback;
        }
        try {
            BigDecimal value = new BigDecimal(raw.trim());
            return value.compareTo(BigDecimal.ZERO) < 0 ? fallback : value;
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private List<AiDemandReferenceVO> toReferences(List<AiContextDocument> documents) {
        return documents.stream()
                .sorted(Comparator.comparingDouble(AiContextDocument::getScore).reversed())
                .map(document -> AiDemandReferenceVO.builder()
                        .sourceType(document.getSourceType())
                        .sourceId(document.getSourceId())
                        .title(document.getTitle())
                        .summary(document.getSummary())
                        .extra(document.getExtra())
                        .linkUrl(document.getLinkUrl())
                        .score(document.getScore())
                        .build())
                .toList();
    }

    private AiDemandDraftVO readCache(String cacheKey) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (!StringUtils.hasText(cached)) {
                return null;
            }
            return objectMapper.readValue(cached, AiDemandDraftVO.class);
        } catch (Exception ex) {
            log.warn("Failed to read AI draft cache: {}", ex.getMessage());
            return null;
        }
    }

    private void writeCache(String cacheKey, AiDemandDraftVO draft, AiRuntimeConfig runtimeConfig) {
        if (runtimeConfig.getCacheTtlSeconds() <= 0) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(draft),
                    runtimeConfig.getCacheTtlSeconds(),
                    TimeUnit.SECONDS
            );
        } catch (Exception ex) {
            log.warn("Failed to write AI draft cache: {}", ex.getMessage());
        }
    }

    private String extractJson(String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.startsWith("```")) {
            int firstBrace = trimmed.indexOf('{');
            int lastBrace = trimmed.lastIndexOf('}');
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                return trimmed.substring(firstBrace, lastBrace + 1);
            }
        }
        return trimmed;
    }

    private String buildCacheFingerprint(AiDemandDraftGenerateDTO request, AiPromptRuntimeConfig promptConfig) {
        return String.join("||",
                normalizeRequirement(request.getRequirement()),
                normalizeRequirement(request.getTitle()),
                normalizeRequirement(request.getSummary()),
                normalizeRequirement(request.getDetail()),
                promptConfig.getPromptVersion(),
                promptConfig.getModel(),
                String.valueOf(promptConfig.getTemperature()),
                String.valueOf(promptConfig.getTopK())
        );
    }

    private String normalizeRequirement(String text) {
        return text == null ? "" : text.replaceAll("\\s+", " ").trim();
    }

    private int resolveTopK(AiPromptRuntimeConfig promptConfig) {
        return Math.max(1, Math.min(promptConfig.getTopK(), 20));
    }

    private Long currentUserId() {
        return UserContextHolder.get() == null ? null : UserContextHolder.get().getUserId();
    }

    private String buildResultPreview(AiDemandDraftVO draft) {
        if (draft == null) {
            return null;
        }
        return String.format(
                "title=%s | category=%s | budget=%s-%s | days=%s | deliveryType=%s",
                safeText(draft.getTitle()),
                safeText(draft.getCategoryName()),
                draft.getBudgetMin(),
                draft.getBudgetMax(),
                draft.getExpectedDays(),
                draft.getDeliveryType()
        );
    }

    private String safeText(String text) {
        return text == null ? "" : text.trim();
    }

    private void maybeEmitStreamProgress(SseEmitter emitter,
                                         AtomicBoolean streamOpen,
                                         AtomicInteger milestoneIndex,
                                         int streamedChars) {
        int currentIndex = milestoneIndex.get();
        while (currentIndex < STREAM_PROGRESS_THRESHOLDS.length && streamedChars >= STREAM_PROGRESS_THRESHOLDS[currentIndex]) {
            boolean sent = sendStatusEvent(
                    emitter,
                    streamOpen,
                    STREAM_PROGRESS_STAGES[currentIndex],
                    STREAM_PROGRESS_MESSAGES[currentIndex],
                    STREAM_PROGRESS_VALUES[currentIndex]
            );
            if (!sent) {
                return;
            }
            if (milestoneIndex.compareAndSet(currentIndex, currentIndex + 1)) {
                currentIndex = milestoneIndex.get();
                continue;
            }
            currentIndex = milestoneIndex.get();
        }
    }

    private boolean sendStatusEvent(SseEmitter emitter, AtomicBoolean streamOpen, String stage, String message, int progress) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("stage", stage);
        payload.put("message", message);
        payload.put("progress", progress);
        return sendStreamEvent(emitter, streamOpen, "status", payload);
    }

    private boolean sendFinalEvent(SseEmitter emitter, AtomicBoolean streamOpen, AiDemandDraftVO draft) {
        return sendStreamEvent(emitter, streamOpen, "final", draft);
    }

    private boolean sendErrorEvent(SseEmitter emitter, AtomicBoolean streamOpen, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", message);
        return sendStreamEvent(emitter, streamOpen, "error", payload);
    }

    private boolean sendStreamEvent(SseEmitter emitter, AtomicBoolean streamOpen, String eventName, Object payload) {
        if (!streamOpen.get()) {
            return false;
        }
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(payload, MediaType.APPLICATION_JSON));
            return true;
        } catch (IOException | IllegalStateException ex) {
            streamOpen.set(false);
            log.debug("AI stream closed while sending {} event: {}", eventName, ex.getMessage());
            return false;
        }
    }

    private void completeEmitter(SseEmitter emitter, AtomicBoolean streamOpen) {
        if (!streamOpen.compareAndSet(true, false)) {
            return;
        }
        try {
            emitter.complete();
        } catch (IllegalStateException ex) {
            log.debug("AI stream emitter already completed: {}", ex.getMessage());
        }
    }

    private static final class AiDemandDraftGenerateDTOBuilder {

        private AiDemandDraftGenerateDTOBuilder() {
        }

        private static AiDemandDraftGenerateDTO copyOf(String requirement, JsonNode root, AiDemandDraftVO fallbackDraft) {
            AiDemandDraftGenerateDTO dto = new AiDemandDraftGenerateDTO();
            dto.setRequirement(requirement);
            dto.setTitle(firstText(root.path("title"), fallbackDraft.getTitle()));
            dto.setSummary(firstText(root.path("summary"), fallbackDraft.getSummary()));
            dto.setDetail(firstText(root.path("detail"), fallbackDraft.getDetail()));
            return dto;
        }

        private static String firstText(JsonNode node, String fallback) {
            String text = node.asText(null);
            return StringUtils.hasText(text) ? text.trim() : fallback;
        }
    }
}
