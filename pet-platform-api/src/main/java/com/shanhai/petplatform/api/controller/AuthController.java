package com.shanhai.petplatform.api.controller;

import com.shanhai.petplatform.common.annotation.RateLimit;
import com.shanhai.petplatform.common.dto.request.LoginRequest;
import com.shanhai.petplatform.common.dto.request.RegisterRequest;
import com.shanhai.petplatform.common.dto.response.LoginVO;
import com.shanhai.petplatform.common.enums.RateLimitType;
import com.shanhai.petplatform.common.result.R;
import com.shanhai.petplatform.infrastructure.security.CurrentUser;
import com.shanhai.petplatform.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证管理 — 登录、注册、验证码、令牌刷新、登出
 *
 * @author PetPlatform Team
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 手机号+密码登录
     */
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO result = authService.login(request);
        return R.ok(result);
    }

    /**
     * 手机号注册
     */
    @PostMapping("/register")
    public R<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        LoginVO result = authService.register(request);
        return R.ok(result);
    }

    /**
     * 发送短信验证码 — 按 IP 限流，防止短信接口被恶意轰炸
     */
    @RateLimit(type = RateLimitType.IP, limit = 5, window = 60, message = "验证码发送过于频繁，请稍后再试")
    @PostMapping("/send-code")
    public R<Void> sendCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        authService.sendCode(phone);
        return R.ok();
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh-token")
    public R<String> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        String newAccessToken = authService.refreshToken(refreshToken);
        return R.ok(newAccessToken);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public R<Void> logout(@CurrentUser Long userId, HttpServletRequest request) {
        authService.logout(userId, extractBearerToken(request));
        return R.ok();
    }

    /**
     * 从请求头提取 Bearer Token（去掉 "Bearer " 前缀）
     */
    private String extractBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}
