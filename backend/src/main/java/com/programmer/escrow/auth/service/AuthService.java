package com.programmer.escrow.auth.service;

import com.programmer.escrow.auth.dto.EmailCodeLoginDTO;
import com.programmer.escrow.auth.dto.EmailCodeSendDTO;
import com.programmer.escrow.auth.dto.LoginDTO;
import com.programmer.escrow.auth.dto.RegisterDTO;
import com.programmer.escrow.auth.dto.UserProfileUpdateDTO;
import com.programmer.escrow.auth.vo.DeveloperProfileVO;
import com.programmer.escrow.auth.vo.LoginVO;
import com.programmer.escrow.user.entity.UserEntity;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    LoginVO register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    void sendEmailLoginCode(EmailCodeSendDTO dto);

    LoginVO loginByEmailCode(EmailCodeLoginDTO dto);

    void logout(HttpServletRequest request);

    LoginVO getLoginProfile(Long userId);

    UserEntity getCurrentUser(Long userId);

    DeveloperProfileVO getDeveloperProfile(Long userId);

    UserEntity updateBasicProfile(Long userId, UserProfileUpdateDTO dto);
}
