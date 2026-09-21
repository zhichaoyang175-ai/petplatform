package com.shanhai.petplatform.infrastructure.aop;

import cn.hutool.core.util.StrUtil;
import com.shanhai.petplatform.common.annotation.RateLimit;
import com.shanhai.petplatform.common.constant.RedisKeyConstant;
import com.shanhai.petplatform.common.enums.RateLimitType;
import com.shanhai.petplatform.common.result.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

/**
 * 接口限流切面 — 拦截标注了 {@link RateLimit} 的方法，执行分布式限流。
 *
 * <p>设计要点：</p>
 * <ul>
 *   <li><b>原子性</b>：采用 Redis Lua 脚本完成「自增 + 首次设置过期」，天然规避 Java 先读后写
 *       在并发下的竞态（读到旧值、重复加锁等问题），保证限流判定原子。</li>
 *   <li><b>分布式</b>：计数存储在 Redis 中心节点，多实例部署下共享同一限流桶，区别于单机内存限流。</li>
 *   <li><b>无侵入</b>：以声明式注解驱动，业务代码零改动，关注点与业务逻辑解耦。</li>
 *   <li><b>Fail-open 降级</b>：Redis 不可用时捕获异常直接放行，保证限流组件不拖垮核心业务，
 *       与项目既有的 Redis 懒连接降级策略保持一致。</li>
 *   <li><b>全局开关</b>：通过 {@code app.rate-limit.enabled} 一键启停，便于灰度与回滚。</li>
 * </ul>
 *
 * @author PetPlatform Team
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    /**
     * 限流 Lua 脚本 — 固定窗口计数：
     * <pre>
     * 1. INCR 原子自增计数；
     * 2. 首次自增（值为 1）时设置过期时间，形成固定时间窗口；
     * 3. 兜底：若 key 因极端情况丢失 TTL（TTL == -1），补设过期，防止 key 永不过期。
     * </pre>
     * 返回当前窗口内已累计的请求次数，由 Java 侧判断是否超过阈值。
     */
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('INCR', KEYS[1])\n" +
            "if current == 1 then\n" +
            "    redis.call('EXPIRE', KEYS[1], ARGV[1])\n" +
            "end\n" +
            "if redis.call('TTL', KEYS[1]) == -1 then\n" +
            "    redis.call('EXPIRE', KEYS[1], ARGV[1])\n" +
            "end\n" +
            "return current",
            Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    /** 限流总开关，默认开启 */
    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    public RateLimitAspect(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // ────────────────── 切面逻辑 ──────────────────

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // 开关关闭时直接放行
        if (!enabled) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String key = buildKey(rateLimit, signature);

        try {
            Long current = stringRedisTemplate.execute(
                    RATE_LIMIT_SCRIPT,
                    Collections.singletonList(key),
                    String.valueOf(rateLimit.window()));

            if (current != null && current > rateLimit.limit()) {
                log.warn("接口触发限流: key={}, count={}, limit={}, window={}s",
                        key, current, rateLimit.limit(), rateLimit.window());
                return R.fail(429, rateLimit.message());
            }
        } catch (Exception e) {
            // Redis 不可用 → fail-open，记录日志后放行，避免限流组件拖垮业务
            log.warn("限流组件执行失败，降级放行: key={}, err={}", key, e.getMessage());
        }

        return joinPoint.proceed();
    }

    // ────────────────── 辅助方法 ──────────────────

    /**
     * 根据限流维度拼装 Redis Key。
     * <p>格式：{@code rate:limit:{prefix}:{dimensionValue}}，
     * 其中 prefix 优先取注解自定义 key，缺省用「类全名.方法名」。</p>
     */
    private String buildKey(RateLimit rateLimit, MethodSignature signature) {
        RateLimitType type = rateLimit.type();
        String dimension;
        switch (type) {
            case USER -> dimension = resolveUserId();
            case INTERFACE -> dimension = signature.getDeclaringTypeName() + "." + signature.getName();
            default -> dimension = resolveClientIp();
        }

        String prefix = StrUtil.isNotBlank(rateLimit.key())
                ? rateLimit.key()
                : signature.getDeclaringTypeName() + "." + signature.getName();

        return RedisKeyConstant.RATE_LIMIT_PREFIX + prefix + ":" + dimension;
    }

    /**
     * 解析客户端真实 IP（兼容反向代理场景，依次尝试 X-Forwarded-For / X-Real-IP）。
     */
    private String resolveClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            int idx = ip.indexOf(',');
            return idx > 0 ? ip.substring(0, idx).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 解析当前登录用户 ID（优先取 JWT 过滤器写入的 request 属性，兜底读 SecurityContext）。
     */
    private String resolveUserId() {
        HttpServletRequest request = getRequest();
        Object attr = request != null ? request.getAttribute("currentUserId") : null;
        if (attr != null) {
            return String.valueOf(attr);
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            return String.valueOf(auth.getPrincipal());
        }
        return "anonymous";
    }

    /**
     * 从 Spring 请求上下文获取当前 HttpServletRequest（非 Web 上下文返回 null）。
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

}
