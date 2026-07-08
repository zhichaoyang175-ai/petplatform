package com.shanhai.petplatform.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件存储策略接口 — 策略模式，支持本地 / OSS 切换
 *
 * @author PetPlatform Team
 */
public interface FileStorageStrategy {

    /**
     * 上传文件
     *
     * @param file   上传的文件
     * @param subDir 子目录（如 "pets/123"）
     * @return 访问 URL（相对路径）
     */
    String upload(MultipartFile file, String subDir);

    /**
     * 删除文件
     *
     * @param fileUrl 文件访问 URL
     */
    void delete(String fileUrl);

    /**
     * 下载文件
     *
     * @param fileUrl 文件访问 URL
     * @return 文件输入流
     */
    InputStream download(String fileUrl);

}
