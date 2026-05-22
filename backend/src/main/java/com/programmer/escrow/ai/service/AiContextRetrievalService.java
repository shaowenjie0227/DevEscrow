package com.programmer.escrow.ai.service;

import com.programmer.escrow.ai.model.AiContextDocument;
import com.programmer.escrow.demand.entity.DemandCategoryEntity;
import com.programmer.escrow.demand.mapper.DemandCategoryMapper;
import com.programmer.escrow.kb.entity.KnowledgeBaseEntity;
import com.programmer.escrow.kb.mapper.KnowledgeBaseMapper;
import com.programmer.escrow.resource.entity.ResourcePostEntity;
import com.programmer.escrow.resource.mapper.ResourcePostMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiContextRetrievalService {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\p{IsAlphabetic}\\p{IsDigit}+#.]{2,}|[\\u4e00-\\u9fff]{2,}");

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final ResourcePostMapper resourcePostMapper;
    private final DemandCategoryMapper demandCategoryMapper;

    public AiContextRetrievalService(KnowledgeBaseMapper knowledgeBaseMapper,
                                     ResourcePostMapper resourcePostMapper,
                                     DemandCategoryMapper demandCategoryMapper) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.resourcePostMapper = resourcePostMapper;
        this.demandCategoryMapper = demandCategoryMapper;
    }

    public List<DemandCategoryEntity> listActiveCategories() {
        return demandCategoryMapper.selectActiveList();
    }

    public List<AiContextDocument> retrieve(String requirement, int limit) {
        String normalizedRequirement = normalize(requirement);
        List<String> keywords = extractKeywords(requirement);
        List<AiContextDocument> ranked = new ArrayList<>();

        for (DemandCategoryEntity category : demandCategoryMapper.selectActiveList()) {
            double score = scoreCategory(normalizedRequirement, keywords, category);
            if (score > 0D) {
                ranked.add(AiContextDocument.builder()
                        .sourceType("CATEGORY")
                        .sourceId(category.getId())
                        .title(category.getCategoryName())
                        .summary(category.getDescription())
                        .extra("需求分类")
                        .score(score)
                        .build());
            }
        }

        for (KnowledgeBaseEntity knowledge : knowledgeBaseMapper.selectActiveList()) {
            double score = score(normalizedRequirement, keywords, knowledge.getTitle(), knowledge.getIntro(), knowledge.getTechName());
            if (score > 0D) {
                ranked.add(AiContextDocument.builder()
                        .sourceType("KNOWLEDGE")
                        .sourceId(knowledge.getId())
                        .title(knowledge.getTitle())
                        .summary(knowledge.getIntro())
                        .extra(knowledge.getTechName())
                        .linkUrl(knowledge.getLinkUrl())
                        .score(score + 0.4D)
                        .build());
            }
        }

        for (ResourcePostEntity resource : resourcePostMapper.selectActiveList()) {
            double score = score(normalizedRequirement, keywords, resource.getTitle(), resource.getIntro());
            if (score > 0D) {
                ranked.add(AiContextDocument.builder()
                        .sourceType("RESOURCE")
                        .sourceId(resource.getId())
                        .title(resource.getTitle())
                        .summary(resource.getIntro())
                        .linkUrl(resource.getLinkUrl())
                        .score(score + 0.2D)
                        .build());
            }
        }

        ranked.sort(Comparator.comparingDouble(AiContextDocument::getScore).reversed()
                .thenComparing(AiContextDocument::getSourceType)
                .thenComparing(AiContextDocument::getSourceId));

        if (!ranked.isEmpty()) {
            return ranked.stream().limit(Math.max(1, limit)).toList();
        }

        return buildDefaultReferences(Math.max(1, limit));
    }

    private List<AiContextDocument> buildDefaultReferences(int limit) {
        List<AiContextDocument> defaults = new ArrayList<>();

        defaults.addAll(demandCategoryMapper.selectActiveList().stream()
                .limit(Math.max(1, limit / 2))
                .map(category -> AiContextDocument.builder()
                        .sourceType("CATEGORY")
                        .sourceId(category.getId())
                        .title(category.getCategoryName())
                        .summary(category.getDescription())
                        .extra("需求分类")
                        .score(0.1D)
                        .build())
                .toList());

        defaults.addAll(knowledgeBaseMapper.selectActiveList().stream()
                .limit(Math.max(1, limit - defaults.size()))
                .map(knowledge -> AiContextDocument.builder()
                        .sourceType("KNOWLEDGE")
                        .sourceId(knowledge.getId())
                        .title(knowledge.getTitle())
                        .summary(knowledge.getIntro())
                        .extra(knowledge.getTechName())
                        .linkUrl(knowledge.getLinkUrl())
                        .score(0.1D)
                        .build())
                .toList());

        if (defaults.size() < limit) {
            defaults.addAll(resourcePostMapper.selectActiveList().stream()
                    .limit(limit - defaults.size())
                    .map(resource -> AiContextDocument.builder()
                            .sourceType("RESOURCE")
                            .sourceId(resource.getId())
                            .title(resource.getTitle())
                            .summary(resource.getIntro())
                            .linkUrl(resource.getLinkUrl())
                            .score(0.1D)
                            .build())
                    .toList());
        }

        return defaults.stream().limit(limit).toList();
    }

    private double scoreCategory(String normalizedRequirement, List<String> keywords, DemandCategoryEntity category) {
        double score = score(normalizedRequirement, keywords, category.getCategoryName(), category.getDescription());
        String categoryName = normalize(category.getCategoryName());
        if (StringUtils.hasText(categoryName) && normalizedRequirement.contains(categoryName)) {
            score += 6D;
        }
        return score;
    }

    private double score(String normalizedRequirement, List<String> keywords, String... texts) {
        double score = 0D;
        for (String text : texts) {
            String normalizedText = normalize(text);
            if (!StringUtils.hasText(normalizedText)) {
                continue;
            }
            if (normalizedRequirement.contains(normalizedText) && normalizedText.length() >= 2) {
                score += Math.min(8D, normalizedText.length() / 2D);
            }
            for (String keyword : keywords) {
                if (normalizedText.contains(keyword)) {
                    score += keyword.length() >= 4 ? 2.5D : 1.2D;
                }
            }
        }
        return score;
    }

    private List<String> extractKeywords(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        Set<String> keywords = new LinkedHashSet<>();
        Matcher matcher = TOKEN_PATTERN.matcher(text.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            String token = matcher.group().trim();
            if (token.length() >= 2) {
                keywords.add(token);
            }
        }
        return List.copyOf(keywords);
    }

    private String normalize(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}
