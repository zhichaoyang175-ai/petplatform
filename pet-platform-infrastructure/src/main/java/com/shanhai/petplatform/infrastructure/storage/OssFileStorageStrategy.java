package com.shanhai.petplatform.infrastructure.storage;

import com.shanhai.petplatform.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 阿里云 OSS 文件存储实现 — 基于 x-file-storage 框架
 *
 * @author PetPlatform Team
 */
@Slf4j
@Component("ossFileStorageStrategy")
@RequiredArgsConstructor
public class OssFileStorageStrategy implements FileStorageStrategy {

    /** 允许的图片扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "webp", "gif"
    ));

    /** 最大文件大小: 10MB */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /** OSS 根路径（与 application-dev.yml 的 base-path 拼接后作为对象 key 前缀） */
    private static final String BASE_PATH = "pet-images/";

    /** OSS 访问域名（与 application-dev.yml 的 dromara.x-file-storage.aliyun-oss[].domain 保持一致）
     *  用于将完整 OSS URL 还原为 bucket 内对象 key。 */
    private static final String OSS_DOMAIN = "https://xingzheshanhai.oss-cn-guangzhou.aliyuncs.com/";

    private final FileStorageService fileStorageService;

    @Override
    public String upload(MultipartFile file, String subDir) {
        validate(file);

        String ext = extractExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        // 路径: petsystem/pets/123/uuid.jpg
        String path = BASE_PATH + subDir + "/";

        FileInfo fileInfo = fileStorageService.of(file)
                .setPath(path)
                .setSaveFilename(filename)
                .upload();

        if (fileInfo == null) {
            throw new BusinessException("OSS 上传失败");
        }

        // 返回 dromara 已生成的完整 OSS URL（含 domain 前缀，如
        // https://xingzheshanhai.oss-cn-guangzhou.aliyuncs.com/petsystem/pet-images/pets/123/uuid.jpg），
        // 前端 <img> 可直接直链加载。
        log.info("OSS 上传成功: {} -> {}", file.getOriginalFilename(), fileInfo.getUrl());
        return fileInfo.getUrl();
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        // 图片字段现在存的是完整 OSS URL，需还原为对象 key 后再删除
        String objectKey = resolveObjectKey(fileUrl);
        if (objectKey == null || objectKey.isBlank()) return;

        try {
            fileStorageService.delete(objectKey);
            log.info("OSS 删除成功: {} (key={})", fileUrl, objectKey);
        } catch (Exception e) {
            log.warn("OSS 删除失败: {} — {}", fileUrl, e.getMessage());
        }
    }

    @Override
    public InputStream download(String fileUrl) {
        // 图片字段现在存的是完整 OSS URL，需还原为对象 key 后再下载
        String objectKey = resolveObjectKey(fileUrl);
        if (objectKey == null || objectKey.isBlank()) {
            throw new BusinessException("无法解析 OSS 文件标识: " + fileUrl);
        }

        try {
            byte[] bytes = fileStorageService.download(objectKey).bytes();
            return new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            throw new BusinessException("OSS 文件读取失败: " + e.getMessage());
        }
    }

    /**
     * 将存储标识解析为 OSS 对象 key（bucket 内相对路径，形如 petsystem/pet-images/pets/123/uuid.jpg）。
     *
     * <p>支持以下输入：
     * <ol>
     *   <li>完整 OSS URL（如 https://xingzheshanhai.oss-cn-guangzhou.aliyuncs.com/petsystem/pet-images/pets/123/uuid.jpg）
     *       → 去掉域名前缀，取路径部分作为对象 key；</li>
     *   <li>遗留的相对路径（如 /api/v1/files/pets/123/uuid.jpg）→ 去掉代理前缀后按历史逻辑拼接 BASE_PATH；</li>
     *   <li>已经是对象 key → 原样返回。</li>
     * </ol>
     *
     * @param fileUrl 文件 URL / 相对路径 / 对象 key
     * @return OSS 对象 key，无法解析时返回原始入参
     */
    private String resolveObjectKey(String fileUrl) {
        if (fileUrl == null) return null;

        // 1) 完整 OSS URL：去掉域名，取路径部分作为对象 key
        if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
            try {
                String path = new URI(fileUrl).getPath();   // /petsystem/pet-images/pets/123/uuid.jpg
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                if (!path.isEmpty()) {
                    return path;
                }
            } catch (URISyntaxException e) {
                log.debug("OSS URL 解析失败，回退到字符串剥离: {}", fileUrl);
            }
            // 回退方案 A：去掉已知域名前缀
            if (fileUrl.startsWith(OSS_DOMAIN)) {
                return fileUrl.substring(OSS_DOMAIN.length());
            }
            // 回退方案 B：按 .aliyuncs.com/ 截断（兼容未配置 domain 的情况）
            int idx = fileUrl.indexOf(".aliyuncs.com/");
            if (idx > 0) {
                return fileUrl.substring(idx + ".aliyuncs.com/".length());
            }
            return fileUrl;
        }

        // 2) 遗留的 /api/v1/files/... 相对路径（兼容 FileController 代理下载），保持历史行为
        if (fileUrl.startsWith("/api/v1/files/")) {
            String relativePath = fileUrl.substring("/api/v1/files/".length());
            return BASE_PATH + relativePath;
        }

        // 3) 兜底：已经是对象 key，原样返回
        return fileUrl;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("上传文件不能为空");
        if (file.getSize() > MAX_FILE_SIZE) throw new BusinessException("文件大小不能超过 10MB");
        String ext = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase()))
            throw new BusinessException("不支持的文件类型: " + ext);
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

}
