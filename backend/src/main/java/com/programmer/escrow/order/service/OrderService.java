package com.programmer.escrow.order.service;

import com.programmer.escrow.order.dto.OrderCreateDTO;
import com.programmer.escrow.order.dto.OrderPayDTO;
import com.programmer.escrow.order.dto.OrderRejectDTO;
import com.programmer.escrow.order.dto.OrderRemarkDTO;
import com.programmer.escrow.order.dto.OrderSubmitDTO;
import com.programmer.escrow.order.vo.OrderDetailVO;
import com.programmer.escrow.order.vo.OrderListVO;

import java.util.List;

public interface OrderService {

    OrderDetailVO createOrder(Long clientId, OrderCreateDTO dto);

    OrderDetailVO payOrder(Long clientId, Long orderId, OrderPayDTO dto);

    OrderDetailVO acceptOrder(Long clientId, Long orderId, OrderRemarkDTO dto);

    OrderDetailVO rejectOrder(Long clientId, Long orderId, OrderRejectDTO dto);

    OrderDetailVO developerAccept(Long developerId, Long orderId, OrderRemarkDTO dto);

    OrderDetailVO developerStart(Long developerId, Long orderId, OrderRemarkDTO dto);

    OrderDetailVO developerSubmit(Long developerId, Long orderId, OrderSubmitDTO dto);

    List<OrderListVO> listClientOrders(Long clientId);

    List<OrderListVO> listDeveloperOrders(Long developerId);

    OrderDetailVO getClientOrderDetail(Long clientId, Long orderId);

    OrderDetailVO getDeveloperOrderDetail(Long developerId, Long orderId);
}
