package com.shanhai.petplatform.common.annotation;

import com.shanhai.petplatform.common.enums.RateLimitType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解 — 声明式限流，标注在 Controller / Service 方法上即可。
 *
 * <p>由 {@code RateLimitAspect} 切面拦截，底层基于 Redis + Lua 脚本实现
 * 固定窗口计数限流，保证并发下「自增 + 过期」的原子性。</p>
 *
 * <p>用法示例：</p>
 * <pre>{@code
 * @RateLimit(type = RateLimitType.IP, limit = 5, window = 60)
 * @PostMapping("/send-code")
 * public R<Void> sendCode(...) { ... }
 * }</pre>
 *
 * @author PetPlatform Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流维度，默认按客户端 IP 限流。
     */
    RateLimitType type() default RateLimitType.IP;

    /**
     * 时间窗口大小（秒），默认 60 秒。
     */
    long window() default 60;

    /**
     * 窗口内允许的最大请求次数，默认 60 次。
     */
    long limit() default 60;

    /**
     * 自定义限流 key 前缀。
     * <p>不填时使用「类全名.方法名」作为前缀；填写时（如 "sms:send"）可跨方法共享同一限流桶。</p>
     */
    String key() default "";

    /**
     * 触发限流时返回给前端的提示信息。
     */
    String message() default "请求过于频繁，请稍后再试";

}
