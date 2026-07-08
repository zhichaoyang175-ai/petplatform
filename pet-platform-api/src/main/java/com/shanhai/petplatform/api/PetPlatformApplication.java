package com.shanhai.petplatform.api;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 宠物领养信息平台 — 启动入口
 *
 * @author PetPlatform Team
 */
@EnableFileStorage
@SpringBootApplication(scanBasePackages = "com.shanhai.petplatform")
@MapperScan({"com.shanhai.petplatform.repository.mapper", "com.shanhai.petplatform.infrastructure.mapper"})
@EnableScheduling
public class PetPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetPlatformApplication.class, args);
    }

}
