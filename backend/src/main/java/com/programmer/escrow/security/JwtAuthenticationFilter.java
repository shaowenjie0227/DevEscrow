package com.programmer.escrow.security;

import com.programmer.escrow.auth.context.LoginUser;
import com.programmer.escrow.auth.context.UserContextHolder;
import com.programmer.escrow.auth.util.AuthRoleResolver;
import com.programmer.escrow.security.model.IssuedJwtToken;
import com.programmer.escrow.security.model.JwtUserClaims;
import com.programmer.escrow.user.entity.UserEntity;
import com.programmer.escrow.user.mapper.UserMapper;
import com.programmer.escrow.user.service.UserBanLifecycleService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtSessionService jwtSessionService;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final UserMapper userMapper;
    private final UserBanLifecycleService userBanLifecycleService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   JwtSessionService jwtSessionService,
                                   RestAuthenticationEntryPoint authenticationEntryPoint,
                                   UserMapper userMapper,
                                   UserBanLifecycleService userBanLifecycleService) {
        this.jwtUtil = jwtUtil;
        this.jwtSessionService = jwtSessionService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.userMapper = userMapper;
        this.userBanLifecycleService = userBanLifecycleService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return requestUri.startsWith("/api/admin/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = jwtUtil.resolveToken(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (jwtSessionService.isBlacklisted(token)) {
                authenticationEntryPoint.writeUnauthorized(response, "jwt has been revoked");
                return;
            }

            JwtUserClaims claims = jwtUtil.parseToken(token);
            UserEntity user = userBanLifecycleService.normalizeBanStatus(userMapper.selectById(claims.userId()));
            if (user == null || user.getStatus() == null || user.getStatus() != 1) {
                authenticationEntryPoint.writeUnauthorized(response, "account has been disabled");
                return;
            }

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            claims.userId(),
                            null,
                            AuthRoleResolver.resolveAuthorities(claims.roles())
                    );
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            UserContextHolder.set(new LoginUser(claims.userId(), claims.userType()));

            if (jwtUtil.shouldRefresh(claims)) {
                IssuedJwtToken refreshedToken = jwtUtil.issueToken(
                        claims.userId(),
                        claims.userType(),
                        claims.nickname(),
                        claims.roles()
                );
                jwtSessionService.storeLoginUserSnapshot(
                        claims.userId(),
                        claims.userType(),
                        claims.nickname(),
                        null,
                        claims.roles(),
                        refreshedToken.jti(),
                        jwtUtil.getRemainingSeconds(refreshedToken.token()),
                        refreshedToken.expiresAt()
                );
                response.setHeader("X-Refresh-Token", refreshedToken.token());
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            authenticationEntryPoint.writeUnauthorized(response, "jwt expired");
        } catch (JwtException | IllegalArgumentException ex) {
            authenticationEntryPoint.writeUnauthorized(response, "jwt invalid");
        } finally {
            UserContextHolder.clear();
            SecurityContextHolder.clearContext();
        }
    }
}
