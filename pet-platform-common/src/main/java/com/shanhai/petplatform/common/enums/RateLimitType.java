package com.shanhai.petplatform.common.enums;

/**
 * 限流维度枚举 — 决定限流桶的 Key 粒度。
 *
 * @author PetPlatform Team
 */
public enum RateLimitType {

    /**
     * 按客户端 IP 限流 — 适用于登录、发送验证码等匿名接口，防止单 IP 恶意刷接口。
     */
    IP,

    /**
     * 按登录用户限流 — 适用于需登录的写接口（如发布宠物），按 userId 隔离限流。
     */
    USER,

    /**
     * 按接口全局限流 — 对整个接口统一限流，不区分调用方。
     */
    INTERFACE

}
