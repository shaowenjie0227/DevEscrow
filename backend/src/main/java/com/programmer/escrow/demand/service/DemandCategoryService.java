package com.programmer.escrow.demand.service;

import com.programmer.escrow.admin.dto.DemandCategorySaveDTO;
import com.programmer.escrow.admin.dto.DemandCategoryStatusDTO;
import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.demand.entity.DemandCategoryEntity;
import com.programmer.escrow.demand.vo.DemandCategoryVO;

import java.util.List;

public interface DemandCategoryService {

    List<DemandCategoryVO> listActiveCategories();

    List<DemandCategoryVO> listAdminCategories();

    DemandCategoryVO createCategory(DemandCategorySaveDTO dto);

    DemandCategoryVO updateCategory(Long categoryId, DemandCategorySaveDTO dto);

    AdminOperationVO updateCategoryStatus(Long categoryId, DemandCategoryStatusDTO dto);

    DemandCategoryEntity getEnabledCategory(Long categoryId);
}
