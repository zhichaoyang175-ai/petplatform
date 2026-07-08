package com.shanhai.petplatform.infrastructure.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储配置 — 根据配置选择存储策略
 *
 * @author PetPlatform Team
 */
@Slf4j
@Configuration
public class FileStorageConfig {

    @Value("${app.file.storage-type:local}")
    private String storageType;

    /**
     * 返回当前激活的文件存储策略
     *
     * @param localFileStorageStrategy 本地存储实现
     * @param ossFileStorageStrategy   OSS 存储实现
     */
    @Bean
    public FileStorageStrategy fileStorageStrategy(
            @Qualifier("localFileStorageStrategy") FileStorageStrategy localFileStorageStrategy,
            @Qualifier("ossFileStorageStrategy") FileStorageStrategy ossFileStorageStrategy) {

        if ("oss".equalsIgnoreCase(storageType)) {
            log.info("文件存储模式: 阿里云 OSS");
            return ossFileStorageStrategy;
        }

        // 默认本地存储
        log.info("文件存储模式: 本地存储");
        return localFileStorageStrategy;
    }

}
