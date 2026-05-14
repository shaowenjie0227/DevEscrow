package com.programmer.escrow.admin.service;

import com.programmer.escrow.admin.vo.AdminUserVO;
import com.programmer.escrow.demand.vo.DemandDetailVO;
import com.programmer.escrow.order.vo.OrderDetailVO;
import com.programmer.escrow.order.vo.OrderListVO;

import java.util.List;

public interface AdminQueryService {

    List<AdminUserVO> listUsers(Integer status, Integer userType, String keyword);

    List<DemandDetailVO> listDemands(Integer reviewStatus, Integer status);

    List<OrderListVO> listOrders(Integer status, String keyword);

    OrderDetailVO getOrderDetail(Long orderId);
}
