package com.programmer.escrow.ai.service;

import com.programmer.escrow.ai.dto.AiDemandDraftGenerateDTO;
import com.programmer.escrow.ai.model.AiContextDocument;
import com.programmer.escrow.ai.vo.AiDemandDraftVO;
import com.programmer.escrow.demand.entity.DemandCategoryEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiDemandDraftFallbackServiceTest {

    private final AiDemandDraftFallbackService aiDemandDraftFallbackService = new AiDemandDraftFallbackService();

    @Test
    void buildDraftShouldGenerateStructuredStagesForComplexRequirement() {
        AiDemandDraftGenerateDTO request = new AiDemandDraftGenerateDTO();
        request.setRequirement("做一个 Java Spring Boot 后台和 Vue 管理端，需要登录权限、订单管理、消息通知，并希望分阶段交付。");

        List<DemandCategoryEntity> categories = List.of(
                category(1L, "Java 后端", "适合 Java、Spring Boot、微服务相关开发"),
                category(2L, "前端开发", "适合 Vue、React、小程序和活动页开发")
        );
        List<AiContextDocument> references = List.of(
                AiContextDocument.builder().sourceType("CATEGORY").sourceId(1L).title("Java 后端").summary("适合接口服务").score(9D).build(),
                AiContextDocument.builder().sourceType("KNOWLEDGE").sourceId(10L).title("Spring Boot 项目交付建议").extra("Spring Boot").summary("适合服务端 API").score(7D).build(),
                AiContextDocument.builder().sourceType("RESOURCE").sourceId(11L).title("Vue3 后台模板资源包").summary("适合管理后台").score(6D).build()
        );

        AiDemandDraftVO draft = aiDemandDraftFallbackService.buildDraft(request, categories, references, "demand-draft-v1");

        assertEquals("Java 后端", draft.getCategoryName());
        assertEquals(2, draft.getDeliveryType());
        assertFalse(draft.getStagePlans().isEmpty());
        assertTrue(draft.getRecommendedSkills().contains("Spring Boot"));
        assertTrue(draft.getRecommendedSkills().contains("RESTful API"));

        BigDecimal stageTotal = draft.getStagePlans().stream()
                .map(stage -> stage.getStageAmount() == null ? BigDecimal.ZERO : stage.getStageAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertTrue(stageTotal.compareTo(draft.getBudgetMin()) >= 0);
        assertTrue(stageTotal.compareTo(draft.getBudgetMax()) <= 0);
    }

    private DemandCategoryEntity category(Long id, String name, String description) {
        DemandCategoryEntity entity = new DemandCategoryEntity();
        entity.setId(id);
        entity.setCategoryName(name);
        entity.setDescription(description);
        entity.setStatus(1);
        return entity;
    }
}
