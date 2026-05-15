package com.programmer.escrow.auth.controller;

import com.programmer.escrow.auth.context.UserContextHolder;
import com.programmer.escrow.auth.dto.LoginDTO;
import com.programmer.escrow.auth.dto.RegisterDTO;
import com.programmer.escrow.auth.service.AuthService;
import com.programmer.escrow.auth.vo.LoginVO;
import com.programmer.escrow.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return ApiResponse.success(authService.register(dto));
    }

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return ApiResponse.success(authService.login(dto));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    public ApiResponse<LoginVO> me() {
        return ApiResponse.success(authService.getLoginProfile(UserContextHolder.getRequiredUserId()));
    }
}
