package com.programmer.escrow.ai.service;

import com.programmer.escrow.ai.dto.AiDemandDraftGenerateDTO;
import com.programmer.escrow.ai.entity.AiPromptTemplateEntity;
import com.programmer.escrow.ai.model.AiContextDocument;
import com.programmer.escrow.ai.model.AiPromptRuntimeConfig;
import com.programmer.escrow.ai.model.AiRuntimeConfig;
import com.programmer.escrow.demand.entity.DemandCategoryEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class AiPromptManager {

    public static final String SCENE_DEMAND_DRAFT = "DEMAND_DRAFT";

    private static final String DEFAULT_PROMPT_VERSION = "demand-draft-default-v1";
    private static final DateTimeFormatter VERSION_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AiPromptTemplateService aiPromptTemplateService;

    public AiPromptManager(AiPromptTemplateService aiPromptTemplateService) {
        this.aiPromptTemplateService = aiPromptTemplateService;
    }

    public AiPromptRuntimeConfig resolveDemandDraftConfig(AiRuntimeConfig runtimeConfig) {
        AiPromptTemplateEntity template = aiPromptTemplateService.getActiveBySceneCode(SCENE_DEMAND_DRAFT);
        if (template == null) {
            return AiPromptRuntimeConfig.builder()
                    .sceneCode(SCENE_DEMAND_DRAFT)
                    .sceneName("需求发布 AI 初稿")
                    .promptVersion(DEFAULT_PROMPT_VERSION)
                    .systemPrompt(defaultSystemPrompt())
                    .userPromptTemplate(null)
                    .model(runtimeConfig.getModel())
                    .temperature(runtimeConfig.getTemperature())
                    .topK(runtimeConfig.getTopK())
                    .custom(false)
                    .build();
        }
        return AiPromptRuntimeConfig.builder()
                .sceneCode(template.getSceneCode())
                .sceneName(template.getSceneName())
                .promptVersion(template.getSceneCode() + "-" + template.getId() + "-" + template.getUpdatedAt().format(VERSION_TIME_FORMATTER))
                .systemPrompt(StringUtils.hasText(template.getSystemPrompt()) ? template.getSystemPrompt() : defaultSystemPrompt())
                .userPromptTemplate(template.getUserPromptTemplate())
                .model(StringUtils.hasText(template.getModel()) ? template.getModel() : runtimeConfig.getModel())
                .temperature(template.getTemperature() == null ? runtimeConfig.getTemperature() : template.getTemperature())
                .topK(template.getTopK() == null ? runtimeConfig.getTopK() : template.getTopK())
                .custom(true)
                .build();
    }

    public String buildSystemPrompt(AiPromptRuntimeConfig config) {
        return StringUtils.hasText(config.getSystemPrompt()) ? config.getSystemPrompt() : defaultSystemPrompt();
    }

    public String buildUserPrompt(AiPromptRuntimeConfig config,
                                  AiDemandDraftGenerateDTO request,
                                  List<DemandCategoryEntity> categories,
                                  List<AiContextDocument> references) {
        if (StringUtils.hasText(config.getUserPromptTemplate())) {
            return renderUserPromptTemplate(config.getUserPromptTemplate(), request, categories, references);
        }
        return defaultUserPrompt(request, categories, references);
    }

    private String renderUserPromptTemplate(String template,
                                            AiDemandDraftGenerateDTO request,
                                            List<DemandCategoryEntity> categories,
                                            List<AiContextDocument> references) {
        return template
                .replace("{{requirement}}", safeText(request.getRequirement()))
                .replace("{{existing_draft}}", buildExistingDraftBlock(request))
                .replace("{{categories}}", buildCategoryBlock(categories))
                .replace("{{references}}", buildReferenceBlock(references))
                .replace("{{rules}}", buildRuleBlock());
    }

    private String defaultSystemPrompt() {
        return """
                你是一名资深的软件项目交付顾问，擅长把模糊需求整理成适合外包接单平台发布的结构化项目单。
                你的输出必须是 JSON，不能包含 markdown、解释性文字或多余前后缀。
                你需要做到：
                1. 帮用户把需求标题、摘要、详细描述整理得更专业、更容易被开发者理解。
                2. 从给定的需求分类里选择最合适的 categoryId 和 categoryName。
                3. 结合需求复杂度，估算预算区间、工期和是否适合拆多阶段交付。
                4. 如果适合多阶段交付，补齐 stagePlans，且每个阶段必须有 stageNo、stageName、stageDesc、stageAmount。
                5. 推荐技能栈 recommendedSkills，并给出 2 到 4 条风险提示 riskTips。
                6. 如果信息不完整，允许基于行业常识做保守推断，但不要虚构与需求明显冲突的内容。
                7. detail 使用自然语言写成适合发布的项目说明，尽量包含：业务目标、功能范围、交付边界、验收建议。
                8. 数值字段必须输出数字，不要输出字符串；urgent 为 false 时 urgentBonus 必须为 0。
                输出 JSON 字段固定为：
                {
                  "title": "",
                  "summary": "",
                  "detail": "",
                  "categoryId": 0,
                  "categoryName": "",
                  "orderType": 1,
                  "urgent": false,
                  "urgentBonus": 0,
                  "budgetMin": 0,
                  "budgetMax": 0,
                  "expectedDays": 0,
                  "deliveryType": 1,
                  "stagePlans": [],
                  "recommendedSkills": [],
                  "riskTips": []
                }
                """;
    }

    private String defaultUserPrompt(AiDemandDraftGenerateDTO request,
                                     List<DemandCategoryEntity> categories,
                                     List<AiContextDocument> references) {
        StringBuilder builder = new StringBuilder();
        builder.append("【用户原始需求】\n");
        builder.append(safeText(request.getRequirement())).append("\n\n");
        builder.append(buildExistingDraftBlock(request)).append("\n");
        builder.append("【可选需求分类】\n");
        builder.append(buildCategoryBlock(categories)).append("\n");
        builder.append("【检索到的参考上下文】\n");
        builder.append(buildReferenceBlock(references)).append("\n");
        builder.append("【补充规则】\n");
        builder.append(buildRuleBlock());
        return builder.toString();
    }

    private String buildExistingDraftBlock(AiDemandDraftGenerateDTO request) {
        StringBuilder builder = new StringBuilder();
        builder.append("【用户当前已填写内容】\n");
        if (hasText(request.getTitle())) {
            builder.append("标题：").append(request.getTitle().trim()).append('\n');
        }
        if (hasText(request.getSummary())) {
            builder.append("摘要：").append(request.getSummary().trim()).append('\n');
        }
        if (hasText(request.getDetail())) {
            builder.append("详情：").append(request.getDetail().trim()).append('\n');
        }
        if (!hasText(request.getTitle()) && !hasText(request.getSummary()) && !hasText(request.getDetail())) {
            builder.append("暂无额外已填写内容。\n");
        }
        return builder.toString();
    }

    private String buildCategoryBlock(List<DemandCategoryEntity> categories) {
        StringBuilder builder = new StringBuilder();
        for (DemandCategoryEntity category : categories) {
            builder.append("- ")
                    .append(category.getId())
                    .append(" | ")
                    .append(category.getCategoryName());
            if (hasText(category.getDescription())) {
                builder.append(" | ").append(category.getDescription().trim());
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    private String buildReferenceBlock(List<AiContextDocument> references) {
        if (references.isEmpty()) {
            return "- 暂无额外参考，请结合用户需求做合理推断。\n";
        }
        StringBuilder builder = new StringBuilder();
        for (AiContextDocument reference : references) {
            builder.append("- [")
                    .append(reference.getSourceType())
                    .append("] ")
                    .append(reference.getTitle());
            if (hasText(reference.getExtra())) {
                builder.append(" | ").append(reference.getExtra().trim());
            }
            if (hasText(reference.getSummary())) {
                builder.append(" | ").append(reference.getSummary().trim());
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    private String buildRuleBlock() {
        return """
                1. 如果需求同时涉及前后台、管理端、交付节点明确，优先考虑 deliveryType=2。
                2. stagePlans 的金额总和必须落在 budgetMin 和 budgetMax 之间。
                3. 如果用户明显在做软件开发需求，orderType 输出 1；如果更像文档、内容、策划类输出 2。
                4. recommendedSkills 优先返回技术名词或岗位技能，不超过 6 个。
                5. riskTips 保持可执行，避免空话。
                """;
    }

    private String safeText(String text) {
        return text == null ? "" : text.trim();
    }

    private boolean hasText(String text) {
        return StringUtils.hasText(text);
    }
}
