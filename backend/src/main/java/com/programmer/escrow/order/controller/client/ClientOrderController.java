package com.programmer.escrow.order.controller.client;

import com.programmer.escrow.auth.context.UserContextHolder;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.order.dto.OrderCreateDTO;
import com.programmer.escrow.order.dto.OrderPayDTO;
import com.programmer.escrow.order.dto.OrderRejectDTO;
import com.programmer.escrow.order.dto.OrderRemarkDTO;
import com.programmer.escrow.order.service.OrderService;
import com.programmer.escrow.order.vo.OrderDetailVO;
import com.programmer.escrow.order.vo.OrderListVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/client/orders")
public class ClientOrderController {

    private final OrderService orderService;

    public ClientOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderDetailVO> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        return ApiResponse.success(orderService.createOrder(UserContextHolder.getRequiredUserId(), dto));
    }

    @PostMapping("/{orderId}/pay")
    public ApiResponse<OrderDetailVO> pay(@PathVariable Long orderId, @Valid @RequestBody OrderPayDTO dto) {
        return ApiResponse.success(orderService.payOrder(UserContextHolder.getRequiredUserId(), orderId, dto));
    }

    @PostMapping("/{orderId}/accept")
    public ApiResponse<OrderDetailVO> accept(@PathVariable Long orderId, @Valid @RequestBody OrderRemarkDTO dto) {
        return ApiResponse.success(orderService.acceptOrder(UserContextHolder.getRequiredUserId(), orderId, dto));
    }

    @PostMapping("/{orderId}/reject")
    public ApiResponse<OrderDetailVO> reject(@PathVariable Long orderId, @Valid @RequestBody OrderRejectDTO dto) {
        return ApiResponse.success(orderService.rejectOrder(UserContextHolder.getRequiredUserId(), orderId, dto));
    }

    @GetMapping
    public ApiResponse<List<OrderListVO>> listOrders() {
        return ApiResponse.success(orderService.listClientOrders(UserContextHolder.getRequiredUserId()));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailVO> getOrderDetail(@PathVariable Long orderId) {
        return ApiResponse.success(orderService.getClientOrderDetail(UserContextHolder.getRequiredUserId(), orderId));
    }
}
