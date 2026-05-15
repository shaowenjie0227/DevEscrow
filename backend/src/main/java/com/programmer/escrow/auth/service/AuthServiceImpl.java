package com.programmer.escrow.auth.service;

import com.programmer.escrow.auth.dto.LoginDTO;
import com.programmer.escrow.auth.dto.RegisterDTO;
import com.programmer.escrow.auth.dto.UserProfileUpdateDTO;
import com.programmer.escrow.auth.util.AuthRoleResolver;
import com.programmer.escrow.auth.vo.DeveloperProfileVO;
import com.programmer.escrow.auth.vo.LoginVO;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.common.util.JsonArrayUtils;
import com.programmer.escrow.infra.sequence.BizNoGenerator;
import com.programmer.escrow.security.JwtSessionService;
import com.programmer.escrow.security.JwtUtil;
import com.programmer.escrow.security.model.IssuedJwtToken;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final BizNoGenerator bizNoGenerator;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtSessionService jwtSessionService;

    public AuthServiceImpl(UserMapper userMapper,
                           BizNoGenerator bizNoGenerator,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           JwtSessionService jwtSessionService) {
        this.userMapper = userMapper;
        this.bizNoGenerator = bizNoGenerator;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.jwtSessionService = jwtSessionService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO dto) {
        if (!StringUtils.hasText(dto.getPhone()) && !StringUtils.hasText(dto.getEmail())) {
            throw new BizException(400, "phone or email is required");
        }
        if (StringUtils.hasText(dto.getPhone()) && userMapper.selectByPhone(dto.getPhone()) != null) {
            throw new BizException(400, "phone already exists");
        }
        if (StringUtils.hasText(dto.getEmail()) && userMapper.selectByEmail(dto.getEmail()) != null) {
            throw new BizException(400, "email already exists");
        }

        UserEntity entity = new UserEntity();
        entity.setUserNo(bizNoGenerator.nextUserNo());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        entity.setNickname(dto.getNickname());
        entity.setUserType(1);
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

        userMapper.updateLastLoginAt(entity.getId());
        return buildLoginResult(userMapper.selectById(entity.getId()));
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        UserEntity entity = userMapper.selectByAccount(dto.getAccount());
        if (entity == null || !passwordEncoder.matches(dto.getPassword(), entity.getPasswordHash())) {
            throw new BizException(401, "account or password is incorrect");
        }
        if (entity.getStatus() == null || entity.getStatus() != 1) {
            throw new BizException(403, "account has been disabled");
        }
        userMapper.updateLastLoginAt(entity.getId());
        return buildLoginResult(entity);
    }

    @Override
    public void logout(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return;
        }
        long remainingSeconds = jwtUtil.getRemainingSeconds(token);
        if (remainingSeconds > 0) {
            jwtSessionService.blacklistToken(token, remainingSeconds);
        }
    }

    @Override
    public LoginVO getLoginProfile(Long userId) {
        return buildLoginProfile(getCurrentUser(userId), null);
    }

    @Override
    public UserEntity getCurrentUser(Long userId) {
        UserEntity entity = userMapper.selectById(userId);
        if (entity == null) {
            throw new BizException(404, "user does not exist");
        }
        return entity;
    }

    @Override
    public DeveloperProfileVO getDeveloperProfile(Long userId) {
        UserEntity entity = getCurrentUser(userId);
        return DeveloperProfileVO.builder()
                .userId(entity.getId())
                .userNo(entity.getUserNo())
                .nickname(entity.getNickname())
                .avatarUrl(entity.getAvatarUrl())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .intro(entity.getIntro())
                .realName(entity.getRealName())
                .idCardNo(entity.getIdCardNo())
                .developerStatus(entity.getDeveloperStatus())
                .idVerifyStatus(entity.getIdVerifyStatus())
                .skillAuditStatus(entity.getSkillAuditStatus())
                .skillAuditReason(entity.getSkillAuditReason())
                .developerRoleType(entity.getDeveloperRoleType())
                .idCardFrontUrl(entity.getIdCardFrontUrl())
                .idCardBackUrl(entity.getIdCardBackUrl())
                .selfieUrl(entity.getSelfieUrl())
                .developerSkillTagIds(JsonArrayUtils.toLongList(entity.getDeveloperSkillTagIds()))
                .build();
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
        var roles = AuthRoleResolver.resolveRoles(entity.getUserType(), entity.getDeveloperStatus());
        IssuedJwtToken issuedJwtToken = jwtUtil.issueToken(
                entity.getId(),
                entity.getUserType(),
                entity.getNickname(),
                roles
        );
        jwtSessionService.storeLoginUser(entity, issuedJwtToken);
        return buildLoginProfile(entity, issuedJwtToken);
    }

    private LoginVO buildLoginProfile(UserEntity entity, IssuedJwtToken issuedJwtToken) {
        var roles = AuthRoleResolver.resolveRoles(entity.getUserType(), entity.getDeveloperStatus());
        return LoginVO.builder()
                .token(issuedJwtToken == null ? null : issuedJwtToken.token())
                .tokenType(issuedJwtToken == null ? null : "Bearer")
                .expiresAt(issuedJwtToken == null ? null : issuedJwtToken.expiresAt())
                .userId(entity.getId())
                .nickname(entity.getNickname())
                .avatarUrl(entity.getAvatarUrl())
                .userType(entity.getUserType())
                .developerStatus(entity.getDeveloperStatus())
                .idVerifyStatus(entity.getIdVerifyStatus())
                .skillAuditStatus(entity.getSkillAuditStatus())
                .developerRoleType(entity.getDeveloperRoleType())
                .skillTags(entity.getSkillTags())
                .roles(roles)
                .redirectPath(AuthRoleResolver.resolveRedirectPath())
                .build();
    }
}
