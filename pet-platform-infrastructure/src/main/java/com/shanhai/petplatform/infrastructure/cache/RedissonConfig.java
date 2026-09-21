package com.shanhai.petplatform.infrastructure.cache;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 客户端配置 — 用于缓存击穿防护的分布式锁。
 *
 * 设计为「Redis 不可用时优雅降级」：若初始化失败（如本地未启动 Redis），
 * 返回 null，业务层 redissonClient 字段为 null，缓存读取自动降级为无锁直查 DB，
 * 不影响应用启动（与项目既有的 Lettuce 懒连接降级策略一致）。
 *
 * @author PetPlatform Team
 */
@Slf4j
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.database:0}")
    private int database;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        try {
            Config config = new Config();
            SingleServerConfig server = config.useSingleServer()
                    .setAddress("redis://" + host + ":" + port)
                    .setDatabase(database)
                    .setConnectTimeout(3000)
                    .setTimeout(3000);
            if (password != null && !password.isBlank()) {
                server.setPassword(password);
            }
            return Redisson.create(config);
        } catch (Exception e) {
            log.warn("Redisson 初始化失败，缓存击穿防护降级为无锁模式（请确认 Redis 已启动）: {}", e.getMessage());
            return null;
        }
    }

}
