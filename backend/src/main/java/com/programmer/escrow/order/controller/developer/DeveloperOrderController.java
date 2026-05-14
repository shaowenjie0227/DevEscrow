package com.programmer.escrow.order.controller.developer;

import com.programmer.escrow.auth.context.UserContextHolder;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.order.dto.OrderRemarkDTO;
import com.programmer.escrow.order.dto.OrderSubmitDTO;
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
@RequestMapping("/api/developer/orders")
public class DeveloperOrderController {

    private final OrderService orderService;

    public DeveloperOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{orderId}/accept")
    public ApiResponse<OrderDetailVO> acceptOrder(@PathVariable Long orderId, @Valid @RequestBody OrderRemarkDTO dto) {
        return ApiResponse.success(orderService.developerAccept(UserContextHolder.getRequiredUserId(), orderId, dto));
    }

    @PostMapping("/{orderId}/start")
    public ApiResponse<OrderDetailVO> startOrder(@PathVariable Long orderId, @Valid @RequestBody OrderRemarkDTO dto) {
        return ApiResponse.success(orderService.developerStart(UserContextHolder.getRequiredUserId(), orderId, dto));
    }

    @PostMapping("/{orderId}/submit")
    public ApiResponse<OrderDetailVO> submitOrder(@PathVariable Long orderId, @Valid @RequestBody OrderSubmitDTO dto) {
        return ApiResponse.success(orderService.developerSubmit(UserContextHolder.getRequiredUserId(), orderId, dto));
    }

    @GetMapping
    public ApiResponse<List<OrderListVO>> listOrders() {
        return ApiResponse.success(orderService.listDeveloperOrders(UserContextHolder.getRequiredUserId()));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailVO> getOrderDetail(@PathVariable Long orderId) {
        return ApiResponse.success(orderService.getDeveloperOrderDetail(UserContextHolder.getRequiredUserId(), orderId));
    }
}
