package com.programmer.escrow.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.programmer.escrow.auth.model.LoginQrState;
import com.programmer.escrow.auth.model.QrLoginStatus;
import com.programmer.escrow.auth.model.WxCallbackResult;
import com.programmer.escrow.auth.util.AuthRoleResolver;
import com.programmer.escrow.auth.vo.QrCodeCreateVO;
import com.programmer.escrow.common.constant.RedisKeys;
import com.programmer.escrow.common.exception.BizException;
import com.programmer.escrow.config.properties.WechatProperties;
import com.programmer.escrow.infra.sequence.BizNoGenerator;
import com.programmer.escrow.loginlog.entity.LoginLogEntity;
import com.programmer.escrow.loginlog.mapper.LoginLogMapper;
import com.programmer.escrow.security.JwtSessionService;
import com.programmer.escrow.security.JwtUtil;
import com.programmer.escrow.security.model.IssuedJwtToken;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import com.programmer.escrow.user.service.UserBanLifecycleService;
import com.programmer.escrow.wechat.model.WechatOAuthUser;
import com.programmer.escrow.wechat.service.WechatOfficialAccountService;
import com.programmer.escrow.websocket.LoginWebSocketSessionManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class QrLoginServiceImpl implements QrLoginService {

    private static final long QR_TTL_SECONDS = 300L;
    private static final long QR_SCAN_LOCK_SECONDS = 90L;

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final WechatOfficialAccountService wechatOfficialAccountService;
    private final WechatProperties wechatProperties;
    private final UserMapper userMapper;
    private final BizNoGenerator bizNoGenerator;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtSessionService jwtSessionService;
    private final LoginLogMapper loginLogMapper;
    private final LoginWebSocketSessionManager loginWebSocketSessionManager;
    private final UserBanLifecycleService userBanLifecycleService;

    public QrLoginServiceImpl(StringRedisTemplate stringRedisTemplate,
                              ObjectMapper objectMapper,
                              WechatOfficialAccountService wechatOfficialAccountService,
                              WechatProperties wechatProperties,
                              UserMapper userMapper,
                              BizNoGenerator bizNoGenerator,
                              PasswordEncoder passwordEncoder,
                              JwtUtil jwtUtil,
                              JwtSessionService jwtSessionService,
                              LoginLogMapper loginLogMapper,
                              LoginWebSocketSessionManager loginWebSocketSessionManager,
                              UserBanLifecycleService userBanLifecycleService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.wechatOfficialAccountService = wechatOfficialAccountService;
        this.wechatProperties = wechatProperties;
        this.userMapper = userMapper;
        this.bizNoGenerator = bizNoGenerator;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.jwtSessionService = jwtSessionService;
        this.loginLogMapper = loginLogMapper;
        this.loginWebSocketSessionManager = loginWebSocketSessionManager;
        this.userBanLifecycleService = userBanLifecycleService;
    }

    @Override
    public QrCodeCreateVO createLoginQr() {
        String token = UUID.randomUUID().toString();
        String loginCode = generateLoginCode();
        Instant now = Instant.now();
        LoginQrState state = LoginQrState.builder()
                .status(QrLoginStatus.WAIT_SCAN)
                .createTime(now)
                .build();
        saveQrState(token, state, QR_TTL_SECONDS);
        stringRedisTemplate.opsForValue().set(
                String.format(RedisKeys.LOGIN_QR_CODE, loginCode),
                token,
                QR_TTL_SECONDS,
                TimeUnit.SECONDS
        );
        return QrCodeCreateVO.builder()
                .token(token)
                .loginCode(loginCode)
                .officialAccountQrImageUrl(wechatProperties.getOfficialAccountQrImageUrl())
                .loginEntryUrl(wechatProperties.getLoginEntryUrl())
                .expireAt(now.plusSeconds(QR_TTL_SECONDS))
                .build();
    }

    @Override
    public String buildWechatAuthorizeUrl(String token) {
        LoginQrState qrState = getRequiredQrState(token);
        if (qrState.getStatus() == QrLoginStatus.SUCCESS) {
            throw new BizException(409, "qr code has already been used");
        }
        if (!acquireScanLock(token)) {
            throw new BizException(409, "qr code is being handled by another device");
        }
        qrState.setStatus(QrLoginStatus.SCANNED);
        qrState.setScanTime(Instant.now());
        saveQrState(token, qrState, getRemainingTtlSeconds(token));
        return wechatOfficialAccountService.buildAuthorizeUrl(token);
    }

    @Override
    public String buildWechatAuthorizeUrlByCode(String loginCode) {
        String token = stringRedisTemplate.opsForValue().get(String.format(RedisKeys.LOGIN_QR_CODE, loginCode));
        if (!StringUtils.hasText(token)) {
            throw new BizException(410, "login code has expired");
        }
        return buildWechatAuthorizeUrl(token);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxCallbackResult handleWechatCallback(String code, String token, String loginIp) {
        LoginQrState qrState = getRequiredQrState(token);
        if (qrState.getStatus() == QrLoginStatus.SUCCESS) {
            return WxCallbackResult.builder()
                    .title("Login completed")
                    .message("The PC site has already been logged in. Please return to your computer.")
                    .build();
        }

        try {
            WechatOAuthUser wechatOAuthUser = wechatOfficialAccountService.authorize(code);
            qrState.setOpenid(wechatOAuthUser.openid());

            if (!wechatOAuthUser.subscribed()) {
                qrState.setStatus(QrLoginStatus.WAIT_FOLLOW);
                saveQrState(token, qrState, getRemainingTtlSeconds(token));
                return WxCallbackResult.builder()
                        .followRequired(true)
                        .followUrl(wechatOfficialAccountService.buildFollowUrl(token))
                        .title("Follow required")
                        .message("Please follow the official account first, then scan the QR code again.")
                        .build();
            }

            UserEntity userEntity = loadOrCreateWechatUser(wechatOAuthUser);
            userEntity = userBanLifecycleService.normalizeBanStatus(userEntity);
            if (userEntity.getStatus() == null || userEntity.getStatus() != 1) {
                throw new BizException(403, "user has been disabled");
            }

            userMapper.updateLastLoginAt(userEntity.getId());
            IssuedJwtToken issuedJwtToken = jwtUtil.issueToken(
                    userEntity.getId(),
                    userEntity.getUserType(),
                    userEntity.getNickname(),
                    AuthRoleResolver.resolveRoles(userEntity.getUserType(), userEntity.getDeveloperStatus())
            );
            jwtSessionService.storeLoginUser(userEntity, issuedJwtToken);

            qrState.setStatus(QrLoginStatus.SUCCESS);
            qrState.setJwt(issuedJwtToken.token());
            qrState.setUserId(userEntity.getId());
            qrState.setSuccessTime(Instant.now());
            saveQrState(token, qrState, Math.max(60L, getRemainingTtlSeconds(token)));

            LoginLogEntity loginLogEntity = new LoginLogEntity();
            loginLogEntity.setUserId(userEntity.getId());
            loginLogEntity.setIp(loginIp);
            loginLogEntity.setLoginTime(LocalDateTime.now());
            loginLogEntity.setLoginType("WECHAT_QR");
            loginLogMapper.insert(loginLogEntity);

            loginWebSocketSessionManager.notifyLoginSuccess(token, issuedJwtToken, userEntity);
            return WxCallbackResult.builder()
                    .title("Login success")
                    .message("PC login succeeded. Please return to your computer.")
                    .build();
        } finally {
            releaseScanLock(token);
        }
    }

    private UserEntity loadOrCreateWechatUser(WechatOAuthUser wechatOAuthUser) {
        UserEntity userEntity = userMapper.selectByOpenid(wechatOAuthUser.openid());
        if (userEntity != null) {
            userEntity.setOpenid(wechatOAuthUser.openid());
            userEntity.setNickname(resolveNickname(wechatOAuthUser.nickname(), userEntity.getNickname()));
            userEntity.setAvatarUrl(resolveAvatar(wechatOAuthUser.avatarUrl(), userEntity.getAvatarUrl()));
            userMapper.updateWechatProfile(userEntity);
            return userMapper.selectById(userEntity.getId());
        }

        UserEntity created = new UserEntity();
        created.setUserNo(bizNoGenerator.nextUserNo());
        created.setOpenid(wechatOAuthUser.openid());
        created.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        created.setNickname(resolveNickname(wechatOAuthUser.nickname(), "WeChat User"));
        created.setAvatarUrl(wechatOAuthUser.avatarUrl());
        created.setUserType(1);
        created.setIdVerifyStatus(0);
        created.setDeveloperStatus(0);
        created.setDeveloperRoleType(0);
        created.setSkillAuditStatus(0);
        created.setRating(BigDecimal.valueOf(5.00));
        created.setCompletedOrderCount(0);
        created.setStatus(1);
        created.setVersion(0);
        created.setCreatedAt(LocalDateTime.now());
        created.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(created);
        return userMapper.selectById(created.getId());
    }

    private String resolveNickname(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    private String resolveAvatar(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    private LoginQrState getRequiredQrState(String token) {
        String json = stringRedisTemplate.opsForValue().get(String.format(RedisKeys.LOGIN_QR, token));
        if (!StringUtils.hasText(json)) {
            throw new BizException(410, "qr code has expired");
        }
        try {
            return objectMapper.readValue(json, LoginQrState.class);
        } catch (IOException ex) {
            throw new IllegalStateException("failed to deserialize qr login state", ex);
        }
    }

    private void saveQrState(String token, LoginQrState qrState, long ttlSeconds) {
        try {
            stringRedisTemplate.opsForValue().set(
                    String.format(RedisKeys.LOGIN_QR, token),
                    objectMapper.writeValueAsString(qrState),
                    Math.max(ttlSeconds, 1L),
                    TimeUnit.SECONDS
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("failed to serialize qr login state", ex);
        }
    }

    private boolean acquireScanLock(String token) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.opsForValue().setIfAbsent(
                        String.format(RedisKeys.LOGIN_QR_SCAN_LOCK, token),
                        "1",
                        QR_SCAN_LOCK_SECONDS,
                        TimeUnit.SECONDS
                )
        );
    }

    private void releaseScanLock(String token) {
        stringRedisTemplate.delete(String.format(RedisKeys.LOGIN_QR_SCAN_LOCK, token));
    }

    private long getRemainingTtlSeconds(String token) {
        Long expireSeconds = stringRedisTemplate.getExpire(String.format(RedisKeys.LOGIN_QR, token), TimeUnit.SECONDS);
        return expireSeconds == null || expireSeconds <= 0 ? 1L : expireSeconds;
    }

    private String generateLoginCode() {
        for (int i = 0; i < 5; i++) {
            String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
            if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(String.format(RedisKeys.LOGIN_QR_CODE, code)))) {
                return code;
            }
        }
        throw new IllegalStateException("failed to allocate unique login code");
    }
}
