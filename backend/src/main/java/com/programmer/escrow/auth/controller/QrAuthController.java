package com.programmer.escrow.auth.controller;

import com.programmer.escrow.auth.service.QrLoginService;
import com.programmer.escrow.auth.vo.QrCodeCreateVO;
import com.programmer.escrow.common.api.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/qr")
public class QrAuthController {

    private final QrLoginService qrLoginService;

    public QrAuthController(QrLoginService qrLoginService) {
        this.qrLoginService = qrLoginService;
    }

    @PostMapping("/create")
    public ApiResponse<QrCodeCreateVO> create() {
        return ApiResponse.success(qrLoginService.createLoginQr());
    }
}
