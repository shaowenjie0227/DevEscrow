package com.programmer.escrow.admin.controller;

import com.programmer.escrow.admin.dto.AdminLoginDTO;
import com.programmer.escrow.admin.service.AdminAuthService;
import com.programmer.escrow.admin.vo.AdminLoginVO;
import com.programmer.escrow.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginVO> login(@Valid @RequestBody AdminLoginDTO dto) {
        return ApiResponse.success(adminAuthService.login(dto));
    }
}
