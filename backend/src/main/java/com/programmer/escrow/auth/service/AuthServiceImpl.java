package com.programmer.escrow.auth.service;

import com.programmer.escrow.auth.dto.LoginDTO;
import com.programmer.escrow.auth.dto.RegisterDTO;
import com.programmer.escrow.auth.dto.UserProfileUpdateDTO;
import com.programmer.escrow.auth.vo.LoginVO;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.infra.redis.TokenCacheService;
import com.programmer.escrow.infra.sequence.BizNoGenerator;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final TokenCacheService tokenCacheService;
    private final BizNoGenerator bizNoGenerator;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.auth.token-expire-seconds}")
    private long tokenExpireSeconds;

    public AuthServiceImpl(UserMapper userMapper,
                           TokenCacheService tokenCacheService,
                           BizNoGenerator bizNoGenerator) {
        this.userMapper = userMapper;
        this.tokenCacheService = tokenCacheService;
        this.bizNoGenerator = bizNoGenerator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO dto) {
        if (!StringUtils.hasText(dto.getPhone()) && !StringUtils.hasText(dto.getEmail())) {
            throw new BizException(400, "手机号或邮箱至少填写一项");
        }
        if (StringUtils.hasText(dto.getPhone()) && userMapper.selectByPhone(dto.getPhone()) != null) {
            throw new BizException(400, "该手机号已注册");
        }
        if (StringUtils.hasText(dto.getEmail()) && userMapper.selectByEmail(dto.getEmail()) != null) {
            throw new BizException(400, "该邮箱已注册");
        }

        UserEntity entity = new UserEntity();
        entity.setUserNo(bizNoGenerator.nextUserNo());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        entity.setNickname(dto.getNickname());
        entity.setUserType(dto.getUserType());
        entity.setIdVerifyStatus(0);
        entity.setDeveloperStatus(0);
        entity.setDeveloperRoleType(0);
        entity.setSkillAuditStatus(0);
        entity.setRating(BigDecimal.valueOf(5.00));
        entity.setCompletedOrderCount(0);
        entity.setStatus(1);
        entity.setVersion(0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(entity);

        if (dto.getUserType() != null && (dto.getUserType() == 2 || dto.getUserType() == 3)) {
          UserEntity developerEntity = userMapper.selectById(entity.getId());
          developerEntity.setRealName(dto.getRealName());
          developerEntity.setDeveloperRoleType(dto.getDeveloperRoleType());
          developerEntity.setIdCardFrontUrl(dto.getIdCardFrontUrl());
          developerEntity.setIdCardBackUrl(dto.getIdCardBackUrl());
          developerEntity.setSelfieUrl(dto.getSelfieUrl());
          developerEntity.setDeveloperSkillTagIds(dto.getSkillTagIds() == null ? null : dto.getSkillTagIds().toString());
          developerEntity.setDeveloperStatus(1);
          developerEntity.setSkillAuditStatus(1);
          developerEntity.setIdVerifyStatus(1);
          userMapper.updateDeveloperProfile(developerEntity);
        }

        userMapper.updateLastLoginAt(entity.getId());
        return buildLoginResult(userMapper.selectById(entity.getId()));
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        UserEntity entity = userMapper.selectByAccount(dto.getAccount());
        if (entity == null || !passwordEncoder.matches(dto.getPassword(), entity.getPasswordHash())) {
            throw new BizException(401, "账号或密码错误");
        }
        if (entity.getStatus() == null || entity.getStatus() != 1) {
            throw new BizException(403, "账号已被禁用");
        }
        userMapper.updateLastLoginAt(entity.getId());
        return buildLoginResult(entity);
    }

    @Override
    public UserEntity getCurrentUser(Long userId) {
        UserEntity entity = userMapper.selectById(userId);
        if (entity == null) {
            throw new BizException(404, "用户不存在");
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserEntity updateBasicProfile(Long userId, UserProfileUpdateDTO dto) {
        UserEntity entity = getCurrentUser(userId);
        entity.setNickname(dto.getNickname());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setIntro(dto.getIntro());
        userMapper.updateBasicProfile(entity);
        return userMapper.selectById(userId);
    }

    private LoginVO buildLoginResult(UserEntity entity) {
        String token = UUID.randomUUID().toString();
        tokenCacheService.storeUserToken(token, entity.getId(), entity.getUserType(), tokenExpireSeconds, entity.getNickname());
        return LoginVO.builder()
                .token(token)
                .userId(entity.getId())
                .nickname(entity.getNickname())
                .userType(entity.getUserType())
                .developerStatus(entity.getDeveloperStatus())
                .idVerifyStatus(entity.getIdVerifyStatus())
                .skillAuditStatus(entity.getSkillAuditStatus())
                .developerRoleType(entity.getDeveloperRoleType())
                .skillTags(entity.getSkillTags())
                .roles(resolveRoles(entity.getUserType()))
                .build();
    }

    private List<String> resolveRoles(Integer userType) {
        List<String> roles = new ArrayList<>();
        if (userType == null) {
            return roles;
        }
        if (userType == 1 || userType == 3) {
            roles.add("CLIENT");
        }
        if (userType == 2 || userType == 3) {
            roles.add("DEVELOPER");
        }
        return roles;
    }
}
