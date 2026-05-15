package com.programmer.escrow.auth.controller;

import com.programmer.escrow.auth.context.UserContextHolder;
import com.programmer.escrow.auth.dto.DeveloperApplyDTO;
import com.programmer.escrow.auth.dto.UserProfileUpdateDTO;
import com.programmer.escrow.auth.service.AuthService;
import com.programmer.escrow.auth.vo.DeveloperProfileVO;
import com.programmer.escrow.common.api.ApiResponse;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/developer/profile")
public class DeveloperAuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    public DeveloperAuthController(AuthService authService, UserMapper userMapper) {
        this.authService = authService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ApiResponse<DeveloperProfileVO> getProfile() {
        return ApiResponse.success(authService.getDeveloperProfile(UserContextHolder.getRequiredUserId()));
    }

    @PutMapping("/basic")
    public ApiResponse<UserEntity> updateBasicProfile(@Valid @RequestBody UserProfileUpdateDTO dto) {
        return ApiResponse.success(authService.updateBasicProfile(UserContextHolder.getRequiredUserId(), dto));
    }

    @PostMapping("/apply")
    public ApiResponse<UserEntity> applyDeveloper(@Valid @RequestBody DeveloperApplyDTO dto) {
        Long userId = UserContextHolder.getRequiredUserId();
        UserEntity entity = authService.getCurrentUser(userId);
        entity.setRealName(dto.getRealName());
        entity.setIdCardNo(dto.getIdCardNo());
        entity.setDeveloperRoleType(dto.getDeveloperRoleType() == null ? 1 : dto.getDeveloperRoleType());
        entity.setIdCardFrontUrl(dto.getIdCardFrontUrl());
        entity.setIdCardBackUrl(dto.getIdCardBackUrl());
        entity.setSelfieUrl(dto.getSelfieUrl());
        entity.setDeveloperStatus(1);
        entity.setSkillAuditStatus(1);
        entity.setIdVerifyStatus(1);
        entity.setSkillAuditReason(null);
        entity.setDeveloperSkillTagIds(dto.getSkillTagIds().toString());
        userMapper.updateDeveloperProfile(entity);
        return ApiResponse.success(entity);
    }
}
