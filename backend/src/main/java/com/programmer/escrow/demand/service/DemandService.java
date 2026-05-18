package com.programmer.escrow.demand.service;

import com.programmer.escrow.demand.dto.DemandCreateDTO;
import com.programmer.escrow.demand.vo.DemandDetailVO;

import java.util.List;

public interface DemandService {

    DemandDetailVO createDemand(Long publisherId, DemandCreateDTO dto);

    List<DemandDetailVO> listMyDemands(Long publisherId);

    List<DemandDetailVO> listMarketDemands();

    DemandDetailVO getMyDemandDetail(Long publisherId, Long demandId);

    DemandDetailVO getMarketDemandDetail(Long demandId);
}
