package com.programmer.escrow.admin.service;

import com.programmer.escrow.admin.dto.AdminLoginDTO;
import com.programmer.escrow.admin.vo.AdminLoginVO;

public interface AdminAuthService {

    AdminLoginVO login(AdminLoginDTO dto);
}
