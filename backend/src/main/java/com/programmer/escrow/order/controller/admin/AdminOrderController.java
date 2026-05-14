package com.programmer.escrow.order.controller.admin;

import com.programmer.escrow.admin.service.AdminQueryService;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.order.vo.OrderDetailVO;
import com.programmer.escrow.order.vo.OrderListVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminQueryService adminQueryService;

    public AdminOrderController(AdminQueryService adminQueryService) {
        this.adminQueryService = adminQueryService;
    }

    @GetMapping
    public ApiResponse<List<OrderListVO>> listOrders(@RequestParam(required = false) Integer status,
                                                     @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminQueryService.listOrders(status, keyword));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailVO> getOrderDetail(@PathVariable Long orderId) {
        return ApiResponse.success(adminQueryService.getOrderDetail(orderId));
    }
}
