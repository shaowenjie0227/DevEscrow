package com.programmer.escrow.dispute.service;

import com.programmer.escrow.dispute.dto.DisputeCreateDTO;
import com.programmer.escrow.dispute.dto.DisputeResolveDTO;
import com.programmer.escrow.dispute.vo.DisputeVO;

import java.util.List;

public interface DisputeService {

    DisputeVO createDispute(Long initiatorId, DisputeCreateDTO dto);

    DisputeVO resolveDispute(Long adminId, Long disputeId, DisputeResolveDTO dto);

    List<DisputeVO> listMyDisputes(Long userId);

    List<DisputeVO> listAllDisputes();
}
