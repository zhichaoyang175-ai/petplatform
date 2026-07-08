package com.shanhai.petplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanhai.petplatform.common.constant.RedisKeyConstant;
import com.shanhai.petplatform.common.dto.request.LoginRequest;
import com.shanhai.petplatform.common.dto.request.RegisterRequest;
import com.shanhai.petplatform.common.dto.response.LoginVO;
import com.shanhai.petplatform.common.exception.BusinessException;
import com.shanhai.petplatform.common.exception.UnauthorizedException;
import com.shanhai.petplatform.infrastructure.cache.CacheService;
import com.shanhai.petplatform.infrastructure.security.JwtTokenProvider;
import com.shanhai.petplatform.repository.entity.User;
import com.shanhai.petplatform.repository.mapper.UserMapper;
import com.shanhai.petplatform.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 *
 * @author PetPlatform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CacheService cacheService;

    private static final Random RANDOM = new Random();

    // ────────────────── 登录 ──────────────────

    @Override
    public LoginVO login(LoginRequest request) {
        // 1. 根据手机号查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));

        // 2. 用户不存在
        if (user == null) {
            throw new BusinessException("手机号未注册");
        }

        // 3. 账号已被禁用
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 4. 密码错误
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 5. 生成令牌
        String accessToken = jwtTokenProvider.generateToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // 6. 将 refreshToken 存入 Redis
        cacheService.set(
                RedisKeyConstant.userTokenKey(user.getId()),
                refreshToken,
                7, TimeUnit.DAYS);

        // 7. 构建 LoginVO 返回
        return LoginVO.of(user.getId(), accessToken, refreshToken,
                user.getRole(), user.getNickname(), user.getAvatarUrl());
    }

    // ────────────────── 注册 ──────────────────

    @Override
    @Transactional
    public LoginVO register(RegisterRequest request) {
        // 1. 检查手机号是否已注册
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        if (count > 0) {
            throw new BusinessException("该手机号已注册");
        }

        // 2. 加密密码
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. 创建用户
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(encodedPassword);
        user.setNickname(request.getNickname());
        // 服务端角色白名单：仅允许 1-领养人 2-送养人，拒绝越权自注册（如 role=3 管理员）
        Integer role = request.getRole();
        if (role == null || (role != 1 && role != 2)) {
            throw new BusinessException("角色非法");
        }
        user.setRole(role);
        user.setStatus(1);
        userMapper.insert(user);

        log.info("新用户注册: phone={}, userId={}, role={}",
                user.getPhone(), user.getId(), user.getRole());

        // 4. 自动登录
        String accessToken = jwtTokenProvider.generateToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        cacheService.set(
                RedisKeyConstant.userTokenKey(user.getId()),
                refreshToken,
                7, TimeUnit.DAYS);

        return LoginVO.of(user.getId(), accessToken, refreshToken,
                user.getRole(), user.getNickname(), user.getAvatarUrl());
    }

    // ────────────────── 短信验证码 ──────────────────

    @Override
    public void sendCode(String phone) {
        // 1. 校验手机号格式
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }

        // 2. 频率限制检查
        if (cacheService.hasKey(RedisKeyConstant.smsRateLimitKey(phone))) {
            throw new BusinessException("验证码发送过于频繁，请60秒后再试");
        }

        // 3. 生成6位随机验证码
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        // 4. 存入 Redis（5分钟过期）
        cacheService.set(
                RedisKeyConstant.smsCodeKey(phone),
                code,
                5, TimeUnit.MINUTES);

        // 5. 设置频率限制（60秒）
        cacheService.set(
                RedisKeyConstant.smsRateLimitKey(phone),
                "1",
                60, TimeUnit.SECONDS);

        // 6. 打印日志（开发环境模拟发送）
        log.info("验证码: {} 已发送至 {}", code, phone);
    }

    // ────────────────── 刷新令牌 ──────────────────

    @Override
    public String refreshToken(String refreshToken) {
        // 1. 校验 refreshToken
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("令牌已过期，请重新登录");
        }

        // 2. 从 token 中获取 userId
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 3. 校验 Redis 中存储的 refreshToken 是否匹配
        String storedToken = cacheService.getString(RedisKeyConstant.userTokenKey(userId));
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new UnauthorizedException("令牌已失效，请重新登录");
        }

        // 4. 生成新的 accessToken
        return jwtTokenProvider.generateToken(userId);
    }

    // ────────────────── 登出 ──────────────────

    @Override
    public void logout(Long userId, String token) {
        // 1. 删除 Redis 中的 refreshToken（使刷新令牌失效）
        cacheService.delete(RedisKeyConstant.userTokenKey(userId));

        // 2. 将当前 accessToken 加入黑名单，TTL=剩余有效期，实现「登出即失效」
        if (token != null && jwtTokenProvider.validateToken(token)) {
            long ttl = jwtTokenProvider.getRemainingExpirationMs(token);
            if (ttl > 0) {
                cacheService.set(RedisKeyConstant.jwtBlacklistKey(token), "1", ttl, TimeUnit.MILLISECONDS);
            }
        }

        log.info("用户登出: userId={}", userId);
    }

}
