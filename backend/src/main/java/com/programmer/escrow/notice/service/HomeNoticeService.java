package com.programmer.escrow.notice.service;

import com.programmer.escrow.admin.vo.AdminOperationVO;
import com.programmer.escrow.notice.dto.HomeNoticeSaveDTO;
import com.programmer.escrow.notice.dto.HomeNoticeStatusDTO;
import com.programmer.escrow.notice.entity.HomeNoticeEntity;
import com.programmer.escrow.notice.vo.HomeNoticeVO;

import java.util.List;

public interface HomeNoticeService {
    List<HomeNoticeVO> listActive();
    HomeNoticeVO getDetail(Long id);
    List<HomeNoticeVO> listAdmin();
    HomeNoticeVO create(HomeNoticeSaveDTO dto);
    HomeNoticeVO update(Long id, HomeNoticeSaveDTO dto);
    AdminOperationVO updateStatus(Long id, HomeNoticeStatusDTO dto);
    AdminOperationVO delete(Long id);
    HomeNoticeEntity getActive(Long id);
}
