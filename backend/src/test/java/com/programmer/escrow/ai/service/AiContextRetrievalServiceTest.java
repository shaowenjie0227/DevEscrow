package com.programmer.escrow.ai.service;

import com.programmer.escrow.ai.model.AiContextDocument;
import com.programmer.escrow.demand.entity.DemandCategoryEntity;
import com.programmer.escrow.demand.mapper.DemandCategoryMapper;
import com.programmer.escrow.kb.entity.KnowledgeBaseEntity;
import com.programmer.escrow.kb.mapper.KnowledgeBaseMapper;
import com.programmer.escrow.resource.entity.ResourcePostEntity;
import com.programmer.escrow.resource.mapper.ResourcePostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiContextRetrievalServiceTest {

    @Mock
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Mock
    private ResourcePostMapper resourcePostMapper;

    @Mock
    private DemandCategoryMapper demandCategoryMapper;

    @InjectMocks
    private AiContextRetrievalService aiContextRetrievalService;

    @Test
    void retrieveShouldReturnRelevantJavaAndSpringContext() {
        when(demandCategoryMapper.selectActiveList()).thenReturn(List.of(
                category(1L, "Java 后端", "适合 Java、Spring Boot、接口服务开发"),
                category(2L, "前端开发", "适合 Vue、React 和活动页开发")
        ));
        when(knowledgeBaseMapper.selectActiveList()).thenReturn(List.of(
                knowledge(10L, "Spring Boot 项目交付建议", "适合服务端 API 和中后台业务", "Spring Boot"),
                knowledge(11L, "Vue3 能做什么", "适合官网和中后台前端", "Vue3")
        ));
        when(resourcePostMapper.selectActiveList()).thenReturn(List.of(
                resource(20L, "Spring Boot 脚手架模板", "适合快速起一个 API 服务"),
                resource(21L, "Vue3 后台模板资源包", "适合管理后台项目")
        ));

        List<AiContextDocument> results = aiContextRetrievalService.retrieve("想做一个 Java Spring Boot 后台和 Vue 管理端", 5);

        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(item -> "CATEGORY".equals(item.getSourceType()) && "Java 后端".equals(item.getTitle())));
        assertTrue(results.stream().anyMatch(item -> "KNOWLEDGE".equals(item.getSourceType()) && "Spring Boot 项目交付建议".equals(item.getTitle())));
        assertTrue(results.stream().anyMatch(item -> "RESOURCE".equals(item.getSourceType()) && item.getTitle().contains("脚手架")));
    }

    private DemandCategoryEntity category(Long id, String name, String description) {
        DemandCategoryEntity entity = new DemandCategoryEntity();
        entity.setId(id);
        entity.setCategoryName(name);
        entity.setDescription(description);
        entity.setStatus(1);
        return entity;
    }

    private KnowledgeBaseEntity knowledge(Long id, String title, String intro, String techName) {
        KnowledgeBaseEntity entity = new KnowledgeBaseEntity();
        entity.setId(id);
        entity.setTitle(title);
        entity.setIntro(intro);
        entity.setTechName(techName);
        entity.setStatus(1);
        return entity;
    }

    private ResourcePostEntity resource(Long id, String title, String intro) {
        ResourcePostEntity entity = new ResourcePostEntity();
        entity.setId(id);
        entity.setTitle(title);
        entity.setIntro(intro);
        entity.setStatus(1);
        return entity;
    }
}
