package com.programmer.escrow.admin.service;

import com.programmer.escrow.admin.dto.AdminLoginDTO;
import com.programmer.escrow.admin.entity.AdminUserEntity;
import com.programmer.escrow.admin.mapper.AdminUserMapper;
import com.programmer.escrow.admin.vo.AdminLoginVO;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.infra.redis.TokenCacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private static final String BOOTSTRAP_HASH_PLACEHOLDER = "$2a$10$replace.with.bcrypt.password";
    private static final String BOOTSTRAP_PASSWORD = "admin123456";

    private final AdminUserMapper adminUserMapper;
    private final TokenCacheService tokenCacheService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.auth.token-expire-seconds}")
    private long tokenExpireSeconds;

    public AdminAuthServiceImpl(AdminUserMapper adminUserMapper, TokenCacheService tokenCacheService) {
        this.adminUserMapper = adminUserMapper;
        this.tokenCacheService = tokenCacheService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminLoginVO login(AdminLoginDTO dto) {
        AdminUserEntity adminUser = adminUserMapper.selectByUsername(dto.getUsername());
        if (adminUser == null) {
            throw new BizException(401, "管理员账号或密码错误");
        }
        if (adminUser.getStatus() == null || adminUser.getStatus() != 1) {
            throw new BizException(403, "管理员账号已被禁用");
        }
        if (!matchesPassword(dto.getPassword(), adminUser.getPasswordHash())) {
            throw new BizException(401, "管理员账号或密码错误");
        }

        adminUserMapper.updateLastLoginAt(adminUser.getId());
        String token = UUID.randomUUID().toString();
        tokenCacheService.storeAdminToken(token, adminUser.getId(), adminUser.getRoleCode(), tokenExpireSeconds, adminUser.getUsername());
        return AdminLoginVO.builder()
                .token(token)
                .adminId(adminUser.getId())
                .username(adminUser.getUsername())
                .realName(adminUser.getRealName())
                .roleCode(adminUser.getRoleCode())
                .build();
    }

    private boolean matchesPassword(String rawPassword, String passwordHash) {
        if (BOOTSTRAP_HASH_PLACEHOLDER.equals(passwordHash)) {
            return BOOTSTRAP_PASSWORD.equals(rawPassword);
        }
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
