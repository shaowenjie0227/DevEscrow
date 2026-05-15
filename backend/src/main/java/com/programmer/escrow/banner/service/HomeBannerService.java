package com.programmer.escrow.banner.service;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.banner.dto.HomeBannerSaveDTO;
import com.programmer.escrow.banner.dto.HomeBannerStatusDTO;
import com.programmer.escrow.banner.entity.HomeBannerEntity;
import com.programmer.escrow.banner.vo.HomeBannerVO;

import java.util.List;

public interface HomeBannerService {
    List<HomeBannerVO> listActive();
    List<HomeBannerVO> listAdmin();
    HomeBannerVO create(HomeBannerSaveDTO dto);
    HomeBannerVO update(Long bannerId, HomeBannerSaveDTO dto);
    AdminOperationVO updateStatus(Long bannerId, HomeBannerStatusDTO dto);
    AdminOperationVO delete(Long bannerId);
    HomeBannerEntity getActiveBanner(Long bannerId);
}
