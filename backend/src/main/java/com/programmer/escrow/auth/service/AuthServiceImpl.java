package com.programmer.escrow.auth.service;

import com.programmer.escrow.auth.dto.EmailCodeLoginDTO;
import com.programmer.escrow.auth.dto.EmailCodeSendDTO;
import com.programmer.escrow.auth.dto.LoginDTO;
import com.programmer.escrow.auth.dto.RegisterDTO;
import com.programmer.escrow.auth.dto.UserProfileUpdateDTO;
import com.programmer.escrow.auth.util.AuthRoleResolver;
import com.programmer.escrow.auth.vo.DeveloperProfileVO;
import com.programmer.escrow.auth.vo.LoginVO;
import com.programmer.escrow.common.constant.RedisKeys;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.common.util.JsonArrayUtils;
import com.programmer.escrow.infra.sequence.BizNoGenerator;
import com.programmer.escrow.infra.mail.MailService;
import com.programmer.escrow.security.JwtSessionService;
import com.programmer.escrow.security.JwtUtil;
import com.programmer.escrow.security.model.IssuedJwtToken;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import com.programmer.escrow.user.service.UserBanLifecycleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Duration EMAIL_LOGIN_CODE_TTL = Duration.ofMinutes(5);
    private static final Duration EMAIL_LOGIN_SEND_INTERVAL = Duration.ofSeconds(60);

    private final UserMapper userMapper;
    private final BizNoGenerator bizNoGenerator;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtSessionService jwtSessionService;
    private final UserBanLifecycleService userBanLifecycleService;
    private final StringRedisTemplate stringRedisTemplate;
    private final MailService mailService;

    public AuthServiceImpl(UserMapper userMapper,
                           BizNoGenerator bizNoGenerator,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           JwtSessionService jwtSessionService,
                           UserBanLifecycleService userBanLifecycleService,
                           StringRedisTemplate stringRedisTemplate,
                           MailService mailService) {
        this.userMapper = userMapper;
        this.bizNoGenerator = bizNoGenerator;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.jwtSessionService = jwtSessionService;
        this.userBanLifecycleService = userBanLifecycleService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.mailService = mailService;
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
        entity = userBanLifecycleService.normalizeBanStatus(entity);
        if (entity.getStatus() == null || entity.getStatus() != 1) {
            throw new BizException(403, "account has been disabled");
        }
        userMapper.updateLastLoginAt(entity.getId());
        return buildLoginResult(entity);
    }

    @Override
    public void sendEmailLoginCode(EmailCodeSendDTO dto) {
        String email = normalizeEmail(dto.getEmail());
        UserEntity entity = loadActiveUserByEmail(email);
        String lockKey = String.format(RedisKeys.LOGIN_EMAIL_CODE_SEND_LOCK, email);
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", EMAIL_LOGIN_SEND_INTERVAL);
        if (!Boolean.TRUE.equals(locked)) {
            long remainingSeconds = resolveRemainingSeconds(lockKey, EMAIL_LOGIN_SEND_INTERVAL.getSeconds());
            throw new BizException(429, "请在 " + remainingSeconds + " 秒后重新获取验证码");
        }

        String codeKey = String.format(RedisKeys.LOGIN_EMAIL_CODE, email);
        String code = generateEmailLoginCode();
        stringRedisTemplate.opsForValue().set(codeKey, code, EMAIL_LOGIN_CODE_TTL);

        try {
            mailService.sendText(
                    entity.getEmail(),
                    "程序员担保交易平台登录验证码",
                    buildEmailLoginCodeContent(code)
            );
        } catch (Exception ex) {
            stringRedisTemplate.delete(codeKey);
            stringRedisTemplate.delete(lockKey);
            throw new BizException(500, "邮箱验证码发送失败，请稍后重试");
        }
    }

    @Override
    public LoginVO loginByEmailCode(EmailCodeLoginDTO dto) {
        String email = normalizeEmail(dto.getEmail());
        UserEntity entity = loadActiveUserByEmail(email);
        String codeKey = String.format(RedisKeys.LOGIN_EMAIL_CODE, email);
        String cachedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (!StringUtils.hasText(cachedCode)) {
            throw new BizException(410, "邮箱验证码已过期，请重新获取");
        }

        if (!cachedCode.equals(dto.getCode().trim())) {
            throw new BizException(401, "邮箱验证码错误");
        }

        stringRedisTemplate.delete(codeKey);
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
        return userBanLifecycleService.normalizeBanStatus(entity);
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

    private UserEntity loadActiveUserByEmail(String email) {
        UserEntity entity = userMapper.selectByEmail(email);
        if (entity == null) {
            throw new BizException(404, "该邮箱尚未注册");
        }
        entity = userBanLifecycleService.normalizeBanStatus(entity);
        if (entity.getStatus() == null || entity.getStatus() != 1) {
            throw new BizException(403, "account has been disabled");
        }
        if (!StringUtils.hasText(entity.getEmail())) {
            throw new BizException(400, "该账号未绑定邮箱");
        }
        return entity;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private long resolveRemainingSeconds(String key, long fallbackSeconds) {
        Long remainingSeconds = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (remainingSeconds == null || remainingSeconds < 1) {
            return fallbackSeconds;
        }
        return remainingSeconds;
    }

    private String generateEmailLoginCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
    }

    private String buildEmailLoginCodeContent(String code) {
        return "您的登录验证码是 " + code + "，5 分钟内有效。\n"
                + "如非本人操作，请忽略这封邮件。";
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
