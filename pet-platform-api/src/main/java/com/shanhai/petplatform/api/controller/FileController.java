package com.shanhai.petplatform.api.controller;

import com.shanhai.petplatform.common.result.R;
import com.shanhai.petplatform.infrastructure.storage.FileStorageStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

/**
 * 文件管理 — 上传与下载
 *
 * @author PetPlatform Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageStrategy fileStorageStrategy;

    /** 通用文件上传 */
    @PostMapping("/upload")
    public R<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = fileStorageStrategy.upload(file, "general");
        return R.ok(Map.of("url", url));
    }

    /** 文件下载 — 支持子目录（/api/v1/files/pets/123/uuid.jpg） */
    @GetMapping("/{path:.+}")
    public ResponseEntity<Resource> download(@PathVariable String path, HttpServletRequest request) {
        // 补全访问路径用于策略查找
        String fullPath = "/api/v1/files/" + path;
        InputStream inputStream = fileStorageStrategy.download(fullPath);

        // 根据扩展名设置 Content-Type
        MediaType mediaType = resolveMediaType(path);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(new InputStreamResource(inputStream));
    }

    /** 根据文件扩展名推断 Content-Type */
    private MediaType resolveMediaType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".webp")) return MediaType.valueOf("image/webp");
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        return MediaType.APPLICATION_OCTET_STREAM;
    }

}
