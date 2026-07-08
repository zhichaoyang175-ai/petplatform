package com.shanhai.petplatform.service;

import com.shanhai.petplatform.common.dto.request.LoginRequest;
import com.shanhai.petplatform.common.dto.request.RegisterRequest;
import com.shanhai.petplatform.common.dto.response.LoginVO;

/**
 * 认证服务接口
 *
 * @author PetPlatform Team
 */
public interface AuthService {

    /**
     * 手机号+密码登录
     */
    LoginVO login(LoginRequest request);

    /**
     * 手机号注册
     */
    LoginVO register(RegisterRequest request);

    /**
     * 发送短信验证码
     */
    void sendCode(String phone);

    /**
     * 刷新令牌
     */
    String refreshToken(String refreshToken);

    /**
     * 登出
     *
     * @param userId 用户ID
     * @param token  当前访问令牌（Bearer 部分），用于将其加入黑名单实现登出即失效
     */
    void logout(Long userId, String token);

}
