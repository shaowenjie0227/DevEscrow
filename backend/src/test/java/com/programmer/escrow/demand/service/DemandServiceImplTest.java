package com.programmer.escrow.demand.service;

import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.demand.dto.DemandCreateDTO;
import com.programmer.escrow.demand.entity.DemandCategoryEntity;
import com.programmer.escrow.demand.entity.DemandEntity;
import com.programmer.escrow.demand.mapper.DemandMapper;
import com.programmer.escrow.demand.model.DemandPayload;
import com.programmer.escrow.demand.util.DemandPayloadUtils;
import com.programmer.escrow.demand.vo.DemandDetailVO;
import com.programmer.escrow.infra.sequence.BizNoGenerator;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemandServiceImplTest {

    @Mock
    private DemandMapper demandMapper;

    @Mock
    private BizNoGenerator bizNoGenerator;

    @Mock
    private DemandCategoryService demandCategoryService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private DemandServiceImpl demandService;

    @Test
    void createDemandShouldPersistExtendedPayloadMetadata() {
        DemandCreateDTO dto = new DemandCreateDTO();
        dto.setTitle("Build a SaaS landing page");
        dto.setSummary("Marketing site with admin content editing");
        dto.setDetail("Need a polished public site and a small CMS");
        dto.setCategoryId(7L);
        dto.setOrderType(2);
        dto.setUrgent(true);
        dto.setUrgentBonus(BigDecimal.valueOf(800));
        dto.setBudgetMin(BigDecimal.valueOf(5000));
        dto.setBudgetMax(BigDecimal.valueOf(8000));
        dto.setExpectedDays(14);
        dto.setDeliveryType(1);

        DemandCategoryEntity category = new DemandCategoryEntity();
        category.setId(7L);
        category.setCategoryName("Website");

        when(demandCategoryService.getEnabledCategory(7L)).thenReturn(category);
        when(bizNoGenerator.nextDemandNo()).thenReturn("DM0001");
        when(userMapper.selectById(101L)).thenReturn(buildPublisher(101L, "Client A"));

        DemandDetailVO result = demandService.createDemand(101L, dto);

        ArgumentCaptor<DemandEntity> entityCaptor = ArgumentCaptor.forClass(DemandEntity.class);
        verify(demandMapper).insert(entityCaptor.capture());
        DemandPayload payload = DemandPayloadUtils.parse(entityCaptor.getValue().getAttachmentsJson());

        assertEquals(2, payload.getOrderType());
        assertTrue(Boolean.TRUE.equals(payload.getUrgent()));
        assertEquals(BigDecimal.valueOf(800), payload.getUrgentBonus());
        assertEquals(2, result.getOrderType());
        assertTrue(Boolean.TRUE.equals(result.getUrgent()));
        assertEquals(BigDecimal.valueOf(800), result.getUrgentBonus());
    }

    @Test
    void createDemandShouldResetUrgentBonusWhenDemandIsNotUrgent() {
        DemandCreateDTO dto = new DemandCreateDTO();
        dto.setTitle("Internal dashboard");
        dto.setSummary("Backoffice data panel");
        dto.setDetail("Read-only dashboard for operations");
        dto.setCategoryId(9L);
        dto.setOrderType(1);
        dto.setUrgent(false);
        dto.setUrgentBonus(BigDecimal.valueOf(500));
        dto.setBudgetMin(BigDecimal.valueOf(3000));
        dto.setBudgetMax(BigDecimal.valueOf(5000));
        dto.setExpectedDays(10);
        dto.setDeliveryType(1);

        DemandCategoryEntity category = new DemandCategoryEntity();
        category.setId(9L);
        category.setCategoryName("Dashboard");

        when(demandCategoryService.getEnabledCategory(9L)).thenReturn(category);
        when(bizNoGenerator.nextDemandNo()).thenReturn("DM0002");
        when(userMapper.selectById(202L)).thenReturn(buildPublisher(202L, "Client B"));

        DemandDetailVO result = demandService.createDemand(202L, dto);

        ArgumentCaptor<DemandEntity> entityCaptor = ArgumentCaptor.forClass(DemandEntity.class);
        verify(demandMapper).insert(entityCaptor.capture());
        DemandPayload payload = DemandPayloadUtils.parse(entityCaptor.getValue().getAttachmentsJson());

        assertFalse(Boolean.TRUE.equals(payload.getUrgent()));
        assertEquals(BigDecimal.ZERO, payload.getUrgentBonus());
        assertFalse(Boolean.TRUE.equals(result.getUrgent()));
        assertEquals(BigDecimal.ZERO, result.getUrgentBonus());
    }

    @Test
    void getMarketDemandDetailShouldHideNonPublicDemand() {
        DemandEntity entity = new DemandEntity();
        entity.setId(88L);
        entity.setReviewStatus(0);
        entity.setStatus(1);

        when(demandMapper.selectById(88L)).thenReturn(entity);

        BizException exception = assertThrows(BizException.class, () -> demandService.getMarketDemandDetail(88L));

        assertEquals(404, exception.getCode());
    }

    private UserEntity buildPublisher(Long id, String nickname) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setNickname(nickname);
        user.setAvatarUrl("https://example.com/avatar.png");
        user.setStatus(1);
        return user;
    }
}
