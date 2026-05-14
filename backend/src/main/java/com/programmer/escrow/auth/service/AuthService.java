package com.programmer.escrow.auth.service;

import com.programmer.escrow.auth.dto.LoginDTO;
import com.programmer.escrow.auth.dto.RegisterDTO;
import com.programmer.escrow.auth.dto.UserProfileUpdateDTO;
import com.programmer.escrow.auth.vo.LoginVO;
import com.programmer.escrow.user.entity.UserEntity;

public interface AuthService {

    LoginVO register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    UserEntity getCurrentUser(Long userId);

    UserEntity updateBasicProfile(Long userId, UserProfileUpdateDTO dto);
}
