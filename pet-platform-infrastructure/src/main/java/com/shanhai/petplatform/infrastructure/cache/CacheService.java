package com.shanhai.petplatform.infrastructure.cache;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务 — 封装 Redis 常用操作，Redis 不可用时静默降级
 *
 * @author PetPlatform Team
 */
@Slf4j
@Service
public class CacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private volatile boolean redisAvailable = true;

    public CacheService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // ────────────────── 写入 ──────────────────

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            stringRedisTemplate.opsForValue().set(key, toJson(value), timeout, unit);
        } catch (Exception e) {
            logRedisError("set", e);
        }
    }

    public void set(String key, Object value) {
        try {
            stringRedisTemplate.opsForValue().set(key, toJson(value));
        } catch (Exception e) {
            logRedisError("set", e);
        }
    }

    /**
     * 仅在 key 不存在时写入（SETNX），用于 MQ 消息幂等去重。
     *
     * <p>返回值语义：{@code true} = 首次写入成功（应继续处理）；{@code false} = key 已存在（重复消息，应跳过）。
     * 当 Redis 异常时<b>返回 true</b>（fail-open）：宁可重复处理，也不因去重组件故障而丢失消息。</p>
     */
    public boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.opsForValue()
                    .setIfAbsent(key, toJson(value), timeout, unit));
        } catch (Exception e) {
            logRedisError("setIfAbsent", e);
            return true;
        }
    }

    // ────────────────── 读取 ──────────────────

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isBlank(json)) return null;
            if (clazz == String.class) return (T) json;
            return JSONUtil.toBean(json, clazz);
        } catch (Exception e) {
            logRedisError("get", e);
            return null;
        }
    }

    public String getString(String key) {
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logRedisError("getString", e);
            return null;
        }
    }

    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            logRedisError("hasKey", e);
            return false;
        }
    }

    // ────────────────── 删除 ──────────────────

    public void delete(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            logRedisError("delete", e);
        }
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.expire(key, timeout, unit));
        } catch (Exception e) {
            logRedisError("expire", e);
            return false;
        }
    }

    // ────────────────── 自增 ──────────────────

    public long increment(String key, long delta) {
        try {
            Long result = stringRedisTemplate.opsForValue().increment(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            logRedisError("increment", e);
            return 0;
        }
    }

    // ────────────────── 工具 ──────────────────

    private String toJson(Object value) {
        if (value instanceof String) return (String) value;
        return JSONUtil.toJsonStr(value);
    }

    private void logRedisError(String operation, Exception e) {
        if (redisAvailable) {
            redisAvailable = false;
            log.warn("Redis 不可用，将使用降级模式（{} 操作失败: {}）", operation, e.getMessage());
        }
    }

}
