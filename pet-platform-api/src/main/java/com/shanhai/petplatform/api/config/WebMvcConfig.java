package com.shanhai.petplatform.api.config;

import com.shanhai.petplatform.infrastructure.security.CurrentUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 配置 — 注册自定义参数解析器 + CORS + 静态资源
 *
 * @author PetPlatform Team
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    // ────────────────── 参数解析器 ──────────────────

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }

    // ────────────────── CORS 跨域 ──────────────────

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // ────────────────── 静态资源映射 ──────────────────

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 本地文件上传目录映射
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }

}
