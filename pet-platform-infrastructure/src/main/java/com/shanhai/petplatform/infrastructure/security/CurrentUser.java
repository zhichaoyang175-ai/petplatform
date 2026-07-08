package com.shanhai.petplatform.infrastructure.security;

import java.lang.annotation.*;

/**
 * 当前登录用户注解 — 用于 Controller 方法参数注入当前用户ID
 *
 * <pre>
 * // 使用示例
 * &#64;GetMapping("/me")
 * public R&lt;UserVO&gt; getProfile(&#64;CurrentUser Long userId) {
 *     return R.ok(userService.getUserById(userId));
 * }
 * </pre>
 *
 * @author PetPlatform Team
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
