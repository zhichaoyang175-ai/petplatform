package com.shanhai.petplatform.infrastructure.storage;

import com.shanhai.petplatform.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 本地文件存储实现
 *
 * @author PetPlatform Team
 */
@Slf4j
@Component("localFileStorageStrategy")
public class LocalFileStorageStrategy implements FileStorageStrategy {

    /** 允许的图片扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "webp", "gif"
    ));

    /** 最大文件大小: 10MB */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Value("${app.file.local-path:./uploads}")
    private String basePath;

    // ────────────────── 上传 ──────────────────

    @Override
    public String upload(MultipartFile file, String subDir) {
        validate(file);

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        // 目标路径: basePath/subDir/newFilename
        Path targetDir = Paths.get(basePath, subDir);
        Path targetPath = targetDir.resolve(newFilename);

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetPath.toFile());
            log.info("文件上传成功: {} -> {}", originalFilename, targetPath);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }

        // 返回相对访问路径
        String normalizedSubDir = subDir.replace("\\", "/");
        return "/api/v1/files/" + normalizedSubDir + "/" + newFilename;
    }

    // ────────────────── 删除 ──────────────────

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        // 从访问路径解析本地路径
        // /api/v1/files/pets/123/uuid.jpg → basePath/pets/123/uuid.jpg
        String relativePath = fileUrl.replace("/api/v1/files/", "");
        Path filePath = Paths.get(basePath, relativePath);

        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("文件删除成功: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("文件删除失败: {} — {}", filePath, e.getMessage());
        }
    }

    // ────────────────── 下载 ──────────────────

    @Override
    public InputStream download(String fileUrl) {
        String relativePath = fileUrl.replace("/api/v1/files/", "");
        Path filePath = Paths.get(basePath, relativePath);

        if (!Files.exists(filePath)) {
            throw new BusinessException(404, "文件不存在: " + relativePath);
        }

        try {
            return new BufferedInputStream(Files.newInputStream(filePath));
        } catch (IOException e) {
            throw new BusinessException("文件读取失败: " + e.getMessage());
        }
    }

    // ────────────────── 校验 ──────────────────

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过 10MB");
        }
        String ext = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new BusinessException("不支持的文件类型: " + ext + "，仅允许 " + ALLOWED_EXTENSIONS);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

}
