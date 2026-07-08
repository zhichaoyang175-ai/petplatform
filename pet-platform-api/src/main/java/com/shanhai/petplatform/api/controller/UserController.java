package com.shanhai.petplatform.api.controller;

import com.shanhai.petplatform.common.dto.request.AdopterApplicationRequest;
import com.shanhai.petplatform.common.dto.response.ReviewTaskVO;
import com.shanhai.petplatform.common.dto.response.UserVO;
import com.shanhai.petplatform.common.enums.UserRoleEnum;
import com.shanhai.petplatform.common.result.R;
import com.shanhai.petplatform.infrastructure.security.CurrentUser;
import com.shanhai.petplatform.repository.entity.User;
import com.shanhai.petplatform.repository.mapper.UserMapper;
import com.shanhai.petplatform.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理 — 个人信息、角色升级
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final ReviewService reviewService;

    /** 获取当前用户信息 */
    @GetMapping("/me")
    public R<UserVO> getProfile(@CurrentUser Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return R.fail(404, "用户不存在");
        return R.ok(UserVO.of(user.getId(), maskPhone(user.getPhone()), user.getNickname(),
                user.getAvatarUrl(), user.getEmail(), user.getRole(), user.getCreatedAt()));
    }

    /** 更新个人信息 */
    @PutMapping("/me")
    public R<UserVO> updateProfile(@CurrentUser Long userId, @RequestBody Map<String, String> body) {
        User user = userMapper.selectById(userId);
        if (user == null) return R.fail(404, "用户不存在");
        if (body.containsKey("nickname")) user.setNickname(body.get("nickname"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));
        if (body.containsKey("realName")) user.setRealName(body.get("realName"));
        userMapper.updateById(user);
        return R.ok(UserVO.of(user.getId(), maskPhone(user.getPhone()), user.getNickname(),
                user.getAvatarUrl(), user.getEmail(), user.getRole(), user.getCreatedAt()));
    }

    /** 申请成为送养人（提交资料进入审核流程，审核通过后才升级为送养人） */
    @PutMapping("/me/adopter-application")
    public R<ReviewTaskVO> submitAdopterApplication(@CurrentUser Long userId,
                                                    @Valid @RequestBody AdopterApplicationRequest req) {
        return R.ok(reviewService.submitAdopterApplication(userId, req));
    }

    /** 查询我的送养人认证申请最新状态 */
    @GetMapping("/me/adopter-application")
    public R<ReviewTaskVO> getAdopterApplication(@CurrentUser Long userId) {
        return R.ok(reviewService.getMyAdopterApplication(userId));
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
