package com.programmer.escrow.admin.service;

import com.programmer.escrow.demand.entity.DemandEntity;
import com.programmer.escrow.demand.mapper.DemandMapper;
import com.programmer.escrow.demand.model.DemandFileItem;
import com.programmer.escrow.demand.model.DemandPayload;
import com.programmer.escrow.demand.util.DemandPayloadUtils;
import com.programmer.escrow.demand.vo.DemandDetailVO;
import com.programmer.escrow.order.mapper.OrderMapper;
import com.programmer.escrow.order.mapper.OrderStageMapper;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminQueryServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private DemandMapper demandMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderStageMapper orderStageMapper;

    @InjectMocks
    private AdminQueryServiceImpl adminQueryService;

    @Test
    void listDemandsShouldIncludePublisherContactAndAssets() {
        DemandEntity entity = new DemandEntity();
        entity.setId(10L);
        entity.setPublisherId(88L);
        entity.setDemandNo("DM202605190001");
        entity.setTitle("后台风控审核页补充资料查看");
        entity.setSummary("需要审核时直接查看发布人的资料和附件");
        entity.setDetail("管理员审核时要能看到账号、联系方式、图片和附件。");
        entity.setCategoryId(6L);
        entity.setCategory("Java 后端");
        entity.setBudgetMin(BigDecimal.valueOf(1000));
        entity.setBudgetMax(BigDecimal.valueOf(3000));
        entity.setExpectedDays(7);
        entity.setDeliveryType(1);
        entity.setQuoteCount(2);
        entity.setReviewStatus(0);
        entity.setStatus(1);
        entity.setAttachmentsJson(buildPayloadJson());

        UserEntity publisher = new UserEntity();
        publisher.setId(88L);
        publisher.setUserNo("U202605190088");
        publisher.setNickname("发布方小王");
        publisher.setRealName("王测试");
        publisher.setPhone("13800138000");
        publisher.setEmail("publisher@example.com");
        publisher.setAvatarUrl("https://example.com/avatar.png");

        when(demandMapper.selectAdminList(null, null)).thenReturn(List.of(entity));
        when(userMapper.selectById(88L)).thenReturn(publisher);

        List<DemandDetailVO> results = adminQueryService.listDemands(null, null);

        assertEquals(1, results.size());
        DemandDetailVO detail = results.get(0);
        assertEquals("发布方小王", detail.getPublisherNickname());
        assertEquals("U202605190088", detail.getPublisherUserNo());
        assertEquals("王测试", detail.getPublisherRealName());
        assertEquals("13800138000", detail.getPublisherPhone());
        assertEquals("publisher@example.com", detail.getPublisherEmail());
        assertEquals(2, detail.getQuoteCount());
        assertEquals(1, detail.getImages().size());
        assertEquals(1, detail.getAttachments().size());
        assertNotNull(detail.getCoverImage());
        assertEquals("https://example.com/spec.pdf", detail.getAttachments().get(0).getUrl());
    }

    private String buildPayloadJson() {
        DemandPayload payload = new DemandPayload();
        payload.setImages(List.of(buildFile("原型图.png", "https://example.com/preview.png", "image/png", 2048L)));
        payload.setAttachments(List.of(buildFile("需求说明书.pdf", "https://example.com/spec.pdf", "application/pdf", 4096L)));
        return DemandPayloadUtils.toJson(payload);
    }

    private DemandFileItem buildFile(String name, String url, String contentType, Long size) {
        DemandFileItem item = new DemandFileItem();
        item.setName(name);
        item.setUrl(url);
        item.setContentType(contentType);
        item.setSize(size);
        return item;
    }
}
