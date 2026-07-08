package com.shanhai.petplatform.infrastructure.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanhai.petplatform.common.constant.RedisKeyConstant;
import com.shanhai.petplatform.infrastructure.cache.CacheService;
import com.shanhai.petplatform.repository.entity.User;
import com.shanhai.petplatform.repository.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * JWT 认证过滤器 — 每个请求进入时校验 Token 并注入认证信息
 *
 * @author PetPlatform Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** 无需认证的路径 */
    private static final Set<String> SKIP_PATHS = Set.of(
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/api-docs/**",
            "/actuator/**",
            "/api/v1/files/**",
            "/api/v1/pets/stats",          // 首页统计数据
            "/favicon.ico"
    );

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final CacheService cacheService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // 跳过白名单路径
        if (isSkipPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 提取 JWT
        String token = extractToken(request);
        if (token == null) {
            log.debug("请求无 JWT: {} {}", request.getMethod(), requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // 校验 JWT
        if (!jwtTokenProvider.validateToken(token)) {
            log.debug("JWT 校验失败: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // 校验是否已登出（加入黑名单的令牌立即失效）
        if (cacheService.hasKey(RedisKeyConstant.jwtBlacklistKey(token))) {
            log.debug("JWT 已登出失效: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // 加载用户并设置认证上下文
        try {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            User user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getId, userId));

            if (user == null || user.getStatus() == 0) {
                log.debug("用户不存在或已禁用: userId={}", userId);
                filterChain.doFilter(request, response);
                return;
            }

            // 构建 Spring Security 认证对象
            List<SimpleGrantedAuthority> authorities = buildAuthorities(user.getRole());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 将 userId 存入 request attribute 供 @CurrentUser 使用
            request.setAttribute("currentUserId", userId);

        } catch (Exception e) {
            log.warn("认证过程异常: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);

        // 请求结束后清理（防止内存泄漏）
        SecurityContextHolder.clearContext();
    }

    // ────────────────── 辅助方法 ──────────────────

    /**
     * 从请求头提取 JWT (格式: Authorization: Bearer <token>)
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 判断是否跳过认证
     */
    private boolean isSkipPath(String path) {
        for (String pattern : SKIP_PATHS) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据 role 构建权限列表
     */
    private List<SimpleGrantedAuthority> buildAuthorities(Integer role) {
        String roleName = switch (role) {
            case 2 -> "ROLE_ADOPTER";
            case 3 -> "ROLE_ADMIN";
            case 4 -> "ROLE_REVIEWER";
            default -> "ROLE_APPLICANT";
        };
        return List.of(new SimpleGrantedAuthority(roleName));
    }

}
