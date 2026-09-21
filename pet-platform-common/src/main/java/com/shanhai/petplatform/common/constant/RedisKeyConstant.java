package com.shanhai.petplatform.common.constant;

/**
 * Redis Key 常量 — 统一管理缓存 Key 前缀，避免硬编码
 *
 * @author PetPlatform Team
 */
public final class RedisKeyConstant {

    private RedisKeyConstant() {}

    /** 短信验证码 — Key: sms:code:{phone}，过期 5 分钟 */
    public static final String SMS_CODE_PREFIX = "sms:code:";

    /** 短信发送频率限制 — Key: sms:limit:{phone}，过期 60 秒 */
    public static final String SMS_RATE_LIMIT_PREFIX = "sms:limit:";

    /** 宠物浏览量计数 — Key: pet:view:{petId} */
    public static final String PET_VIEW_COUNT_PREFIX = "pet:view:";

    /** 热门宠物列表 — Key: pets:hot，过期 1 小时 */
    public static final String HOT_PETS_KEY = "pets:hot";

    /** 用户 token 黑名单 — Key: user:token:{userId}，用于登出 */
    public static final String USER_TOKEN_PREFIX = "user:token:";

    /** 访问令牌黑名单 — Key: jwt:blacklist:{token}，用于登出即失效，TTL=令牌剩余有效期 */
    public static final String JWT_BLACKLIST_PREFIX = "jwt:blacklist:";

    /** 接口限流计数 — Key: rate:limit:{prefix}:{dimension}，TTL=限流窗口时长 */
    public static final String RATE_LIMIT_PREFIX = "rate:limit:";

    /** MQ 消息幂等去重 — Key: mq:dedup:{业务标识}，TTL 24 小时，用于消费者防重复投递 */
    public static final String MQ_DEDUP_PREFIX = "mq:dedup:";

    // ────────────────── 帮助方法 ──────────────────

    public static String smsCodeKey(String phone) {
        return SMS_CODE_PREFIX + phone;
    }

    public static String smsRateLimitKey(String phone) {
        return SMS_RATE_LIMIT_PREFIX + phone;
    }

    public static String petViewKey(Long petId) {
        return PET_VIEW_COUNT_PREFIX + petId;
    }

    public static String userTokenKey(Long userId) {
        return USER_TOKEN_PREFIX + userId;
    }

    public static String jwtBlacklistKey(String token) {
        return JWT_BLACKLIST_PREFIX + token;
    }

    /**
     * MQ 通知消息幂等去重 Key — 按「关联业务 + 类型」唯一标识一条通知。
     */
    public static String mqNotificationDedupKey(String refType, Long refId, Integer type) {
        return MQ_DEDUP_PREFIX + "notification:" + refType + ":" + refId + ":" + type;
    }

}
