package com.programmer.escrow.ai.service;

import com.programmer.escrow.ai.dto.AiDemandDraftGenerateDTO;
import com.programmer.escrow.ai.model.AiContextDocument;
import com.programmer.escrow.ai.vo.AiDemandDraftStageVO;
import com.programmer.escrow.ai.vo.AiDemandDraftVO;
import com.programmer.escrow.demand.entity.DemandCategoryEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AiDemandDraftFallbackService {

    public AiDemandDraftVO buildDraft(AiDemandDraftGenerateDTO request,
                                      List<DemandCategoryEntity> categories,
                                      List<AiContextDocument> references,
                                      String promptVersion) {
        String requirement = sanitize(request.getRequirement());
        DemandCategoryEntity matchedCategory = matchCategory(requirement, categories, references);

        int complexity = estimateComplexity(requirement);
        boolean urgent = containsAny(requirement, "加急", "尽快", "本周", "本月内", "紧急");
        int deliveryType = shouldSplitStages(requirement, complexity) ? 2 : 1;
        int orderType = containsAny(requirement, "文档", "方案", "文章", "策划", "梳理") ? 2 : 1;
        int expectedDays = Math.max(5, 4 + complexity * 4);

        BigDecimal budgetMin = BigDecimal.valueOf(2000L + complexity * 1500L);
        BigDecimal budgetMax = budgetMin.add(BigDecimal.valueOf(Math.max(2000L, complexity * 2200L)));
        BigDecimal urgentBonus = urgent
                ? budgetMin.multiply(BigDecimal.valueOf(0.1D)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        String title = firstNonBlank(request.getTitle(), buildTitle(requirement, matchedCategory));
        String summary = firstNonBlank(request.getSummary(), buildSummary(requirement));
        List<String> skills = buildSkills(matchedCategory, references, requirement);
        List<String> risks = buildRiskTips(requirement, deliveryType, urgent);
        String detail = firstNonBlank(request.getDetail(), buildDetail(requirement, skills, deliveryType));

        return AiDemandDraftVO.builder()
                .title(title)
                .summary(summary)
                .detail(detail)
                .categoryId(matchedCategory == null ? null : matchedCategory.getId())
                .categoryName(matchedCategory == null ? null : matchedCategory.getCategoryName())
                .orderType(orderType)
                .urgent(urgent)
                .urgentBonus(urgentBonus)
                .budgetMin(budgetMin.setScale(2, RoundingMode.HALF_UP))
                .budgetMax(budgetMax.setScale(2, RoundingMode.HALF_UP))
                .expectedDays(expectedDays)
                .deliveryType(deliveryType)
                .stagePlans(deliveryType == 2 ? buildStagePlans(budgetMin, budgetMax, expectedDays, requirement) : List.of())
                .recommendedSkills(skills)
                .riskTips(risks)
                .fromCache(false)
                .fallbackUsed(true)
                .promptVersion(promptVersion)
                .build();
    }

    private DemandCategoryEntity matchCategory(String requirement,
                                               List<DemandCategoryEntity> categories,
                                               List<AiContextDocument> references) {
        String normalizedRequirement = requirement.toLowerCase(Locale.ROOT);

        for (AiContextDocument reference : references) {
            if (!"CATEGORY".equals(reference.getSourceType())) {
                continue;
            }
            for (DemandCategoryEntity category : categories) {
                if (category.getId() != null && category.getId().equals(reference.getSourceId())) {
                    return category;
                }
            }
        }

        for (DemandCategoryEntity category : categories) {
            String categoryName = category.getCategoryName() == null ? "" : category.getCategoryName().toLowerCase(Locale.ROOT);
            String description = category.getDescription() == null ? "" : category.getDescription().toLowerCase(Locale.ROOT);
            if ((!categoryName.isEmpty() && normalizedRequirement.contains(categoryName))
                    || (!description.isEmpty() && normalizedRequirement.contains(description))) {
                return category;
            }
        }

        if (containsAny(normalizedRequirement, "java", "spring", "接口", "后端")) {
            return findCategory(categories, "java", "后端");
        }
        if (containsAny(normalizedRequirement, "vue", "react", "前端", "h5", "小程序")) {
            return findCategory(categories, "前端", "ui");
        }
        if (containsAny(normalizedRequirement, "android", "ios", "flutter", "uniapp", "移动")) {
            return findCategory(categories, "移动", "app");
        }
        return categories.isEmpty() ? null : categories.get(0);
    }

    private DemandCategoryEntity findCategory(List<DemandCategoryEntity> categories, String... keywords) {
        for (DemandCategoryEntity category : categories) {
            String haystack = ((category.getCategoryName() == null ? "" : category.getCategoryName()) + " "
                    + (category.getDescription() == null ? "" : category.getDescription())).toLowerCase(Locale.ROOT);
            for (String keyword : keywords) {
                if (haystack.contains(keyword.toLowerCase(Locale.ROOT))) {
                    return category;
                }
            }
        }
        return categories.isEmpty() ? null : categories.get(0);
    }

    private int estimateComplexity(String requirement) {
        int score = 1;
        if (requirement.length() > 80) {
            score += 1;
        }
        if (requirement.length() > 160) {
            score += 1;
        }
        if (containsAny(requirement, "前端", "后台", "管理端", "接口", "支付", "消息", "登录", "权限", "报表")) {
            score += 1;
        }
        if (containsAny(requirement, "同时", "另外", "还需要", "多角色", "多端", "小程序", "App")) {
            score += 1;
        }
        return Math.max(1, Math.min(score, 5));
    }

    private boolean shouldSplitStages(String requirement, int complexity) {
        return complexity >= 3 || containsAny(requirement, "阶段", "一期", "二期", "先做", "分步");
    }

    private String buildTitle(String requirement, DemandCategoryEntity category) {
        String compact = requirement.replaceAll("\\s+", " ").trim();
        if (compact.length() > 26) {
            compact = compact.substring(0, 26) + "...";
        }
        String prefix = category == null || !StringUtils.hasText(category.getCategoryName())
                ? "项目需求整理"
                : category.getCategoryName() + "需求";
        return prefix + "：" + compact;
    }

    private String buildSummary(String requirement) {
        String compact = requirement.replaceAll("\\s+", " ").trim();
        if (compact.length() > 56) {
            compact = compact.substring(0, 56) + "...";
        }
        return "希望基于当前想法整理出清晰的功能范围、交付边界、预算与工期，便于开发者快速评估并报价。原始诉求：" + compact;
    }

    private String buildDetail(String requirement, List<String> skills, int deliveryType) {
        StringBuilder builder = new StringBuilder();
        builder.append("业务目标：\n");
        builder.append("希望围绕以下需求完成一套可交付方案，并让开发者能够快速理解目标与范围：").append(requirement).append("\n\n");
        builder.append("功能范围：\n");
        builder.append("1. 结合原始描述梳理核心页面、关键流程与基础数据结构。\n");
        builder.append("2. 明确本期必须完成的主功能，减少模糊描述带来的沟通成本。\n");
        if (deliveryType == 2) {
            builder.append("3. 建议按阶段推进，先完成需求澄清与核心能力，再逐步补齐优化项与交付材料。\n\n");
        } else {
            builder.append("3. 建议优先保障主流程跑通，再根据预算决定是否扩展边缘能力。\n\n");
        }
        builder.append("技术建议：\n");
        builder.append("建议优先评估以下能力是否需要覆盖：").append(String.join("、", skills)).append("。\n\n");
        builder.append("验收建议：\n");
        builder.append("需明确交付清单、运行方式、测试范围和关键截图/文档，避免仅完成页面而无法实际使用。");
        return builder.toString();
    }

    private List<String> buildSkills(DemandCategoryEntity category, List<AiContextDocument> references, String requirement) {
        Set<String> skills = new LinkedHashSet<>();
        if (category != null && StringUtils.hasText(category.getCategoryName())) {
            skills.add(category.getCategoryName());
        }
        for (AiContextDocument reference : references) {
            if (StringUtils.hasText(reference.getExtra())) {
                skills.add(reference.getExtra().trim());
            }
            if (StringUtils.hasText(reference.getTitle())) {
                String title = reference.getTitle().trim();
                if (title.contains("Spring Boot")) {
                    skills.add("Spring Boot");
                }
                if (title.contains("Vue")) {
                    skills.add("Vue 3");
                }
            }
        }
        if (containsAny(requirement, "java", "spring")) {
            skills.add("Spring Boot");
        }
        if (containsAny(requirement, "vue", "前端", "h5")) {
            skills.add("Vue 3");
        }
        skills.add("RESTful API");
        skills.add("需求分析");
        return new ArrayList<>(skills).stream().limit(6).toList();
    }

    private List<String> buildRiskTips(String requirement, int deliveryType, boolean urgent) {
        List<String> risks = new ArrayList<>();
        risks.add("请先确认本期必须交付的核心功能，避免需求持续膨胀导致预算和工期失真。");
        risks.add("建议在开发前补充页面原型、接口示意或参考产品，减少理解偏差。");
        if (deliveryType == 2) {
            risks.add("分阶段交付时要提前约定每个阶段的验收标准和可回滚边界。");
        }
        if (urgent) {
            risks.add("加急项目通常需要预留更高沟通频次和更严格的变更控制。");
        }
        if (containsAny(requirement, "支付", "登录", "权限", "微信")) {
            risks.add("涉及账号、权限或支付能力时，需要单独确认第三方对接条件和安全要求。");
        }
        return risks.stream().distinct().limit(4).toList();
    }

    private List<AiDemandDraftStageVO> buildStagePlans(BigDecimal budgetMin,
                                                       BigDecimal budgetMax,
                                                       int expectedDays,
                                                       String requirement) {
        BigDecimal total = budgetMin.add(budgetMax)
                .divide(BigDecimal.valueOf(2L), 2, RoundingMode.HALF_UP);
        boolean largeScope = expectedDays >= 12 || containsAny(requirement, "后台", "前端", "管理端", "多角色", "多端");

        if (!largeScope) {
            BigDecimal firstAmount = total.multiply(BigDecimal.valueOf(0.4D)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal secondAmount = total.subtract(firstAmount).setScale(2, RoundingMode.HALF_UP);
            return List.of(
                    AiDemandDraftStageVO.builder()
                            .stageNo(1)
                            .stageName("需求澄清与核心开发")
                            .stageDesc("明确需求边界，完成核心功能开发与主要页面联调。")
                            .stageAmount(firstAmount)
                            .build(),
                    AiDemandDraftStageVO.builder()
                            .stageNo(2)
                            .stageName("测试优化与交付")
                            .stageDesc("完成测试修复、交付文档整理和上线支持。")
                            .stageAmount(secondAmount)
                            .build()
            );
        }

        BigDecimal firstAmount = total.multiply(BigDecimal.valueOf(0.3D)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal secondAmount = total.multiply(BigDecimal.valueOf(0.4D)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal thirdAmount = total.subtract(firstAmount).subtract(secondAmount).setScale(2, RoundingMode.HALF_UP);
        return List.of(
                AiDemandDraftStageVO.builder()
                        .stageNo(1)
                        .stageName("需求梳理与方案设计")
                        .stageDesc("梳理业务流程、页面结构、接口边界，并确认本期交付清单。")
                        .stageAmount(firstAmount)
                        .build(),
                AiDemandDraftStageVO.builder()
                        .stageNo(2)
                        .stageName("核心功能开发")
                        .stageDesc("完成主要功能、核心页面和关键接口联调，形成可演示版本。")
                        .stageAmount(secondAmount)
                        .build(),
                AiDemandDraftStageVO.builder()
                        .stageNo(3)
                        .stageName("联调测试与上线交付")
                        .stageDesc("处理测试问题，补齐交付说明、部署材料并协助完成验收。")
                        .stageAmount(thirdAmount)
                        .build()
        );
    }

    private boolean containsAny(String text, String... keywords) {
        String normalized = text == null ? "" : text.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String firstNonBlank(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred.trim() : fallback;
    }

    private String sanitize(String text) {
        return text == null ? "" : text.replaceAll("\\s+", " ").trim();
    }
}
