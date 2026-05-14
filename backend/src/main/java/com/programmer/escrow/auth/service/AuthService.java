package com.programmer.escrow.auth.service;

import com.programmer.escrow.auth.dto.LoginDTO;
import com.programmer.escrow.auth.dto.RegisterDTO;
import com.programmer.escrow.auth.vo.LoginVO;

public interface AuthService {

    LoginVO register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);
}
