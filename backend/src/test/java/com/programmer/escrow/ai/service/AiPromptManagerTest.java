package com.programmer.escrow.ai.service;

import com.programmer.escrow.ai.dto.AiDemandDraftGenerateDTO;
import com.programmer.escrow.ai.entity.AiPromptTemplateEntity;
import com.programmer.escrow.ai.model.AiContextDocument;
import com.programmer.escrow.ai.model.AiPromptRuntimeConfig;
import com.programmer.escrow.ai.model.AiRuntimeConfig;
import com.programmer.escrow.demand.entity.DemandCategoryEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiPromptManagerTest {

    @Mock
    private AiPromptTemplateService aiPromptTemplateService;

    @InjectMocks
    private AiPromptManager aiPromptManager;

    @Test
    void resolveDemandDraftConfigShouldFallbackToDefaultWhenNoTemplateExists() {
        AiRuntimeConfig runtimeConfig = AiRuntimeConfig.builder()
                .model("gpt-4.1-mini")
                .temperature(0.2D)
                .topK(6)
                .build();

        when(aiPromptTemplateService.getActiveBySceneCode(AiPromptManager.SCENE_DEMAND_DRAFT)).thenReturn(null);

        AiPromptRuntimeConfig config = aiPromptManager.resolveDemandDraftConfig(runtimeConfig);

        assertFalse(config.isCustom());
        assertEquals(AiPromptManager.SCENE_DEMAND_DRAFT, config.getSceneCode());
        assertEquals("gpt-4.1-mini", config.getModel());
        assertEquals(6, config.getTopK());
        assertTrue(config.getSystemPrompt().contains("JSON"));
    }

    @Test
    void buildUserPromptShouldRenderCustomTemplatePlaceholders() {
        AiRuntimeConfig runtimeConfig = AiRuntimeConfig.builder()
                .model("gpt-4.1-mini")
                .temperature(0.2D)
                .topK(6)
                .build();

        AiPromptTemplateEntity template = new AiPromptTemplateEntity();
        template.setId(3L);
        template.setSceneCode(AiPromptManager.SCENE_DEMAND_DRAFT);
        template.setSceneName("需求发布 AI 初稿");
        template.setUserPromptTemplate("""
                REQ={{requirement}}
                EXIST={{existing_draft}}
                CATEGORIES={{categories}}
                REFERENCES={{references}}
                RULES={{rules}}
                """);
        template.setModel("gpt-4.1");
        template.setTemperature(0.4D);
        template.setTopK(8);
        template.setStatus(1);
        template.setUpdatedAt(LocalDateTime.of(2026, 5, 21, 12, 0, 0));

        when(aiPromptTemplateService.getActiveBySceneCode(AiPromptManager.SCENE_DEMAND_DRAFT)).thenReturn(template);

        AiPromptRuntimeConfig config = aiPromptManager.resolveDemandDraftConfig(runtimeConfig);

        AiDemandDraftGenerateDTO request = new AiDemandDraftGenerateDTO();
        request.setRequirement("做一个 Spring Boot 后台和 Vue 管理端");
        request.setTitle("后台管理系统");

        DemandCategoryEntity category = new DemandCategoryEntity();
        category.setId(1L);
        category.setCategoryName("Java 后端");
        category.setDescription("适合 Java 和 Spring Boot 开发");

        List<AiContextDocument> references = List.of(
                AiContextDocument.builder()
                        .sourceType("KNOWLEDGE")
                        .sourceId(10L)
                        .title("Spring Boot 项目交付建议")
                        .summary("适合服务端 API")
                        .extra("Spring Boot")
                        .score(9D)
                        .build()
        );

        String prompt = aiPromptManager.buildUserPrompt(config, request, List.of(category), references);

        assertTrue(config.isCustom());
        assertEquals("gpt-4.1", config.getModel());
        assertEquals(8, config.getTopK());
        assertTrue(prompt.contains("REQ=做一个 Spring Boot 后台和 Vue 管理端"));
        assertTrue(prompt.contains("Java 后端"));
        assertTrue(prompt.contains("Spring Boot 项目交付建议"));
        assertTrue(prompt.contains("RULES="));
    }
}
