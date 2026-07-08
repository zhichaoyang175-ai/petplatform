package com.shanhai.petplatform.infrastructure.storage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.recorder.FileRecorder;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.springframework.stereotype.Component;

/**
 * dromara x-file-storage 的 {@link FileRecorder} 实现。
 *
 * <p>框架在上传/更新/删除文件时会回调本类的 6 个方法，用于将文件元信息落库。
 * 以 {@code url} 作为唯一检索键（dromara 实际在 delete / getByUrl 时传入的就是 URL）。</p>
 *
 * @author PetPlatform Team
 */
@Component
@RequiredArgsConstructor
public class FileRecorderImpl implements FileRecorder {

    private final FileInfoMapper fileInfoMapper;

    @Override
    public boolean save(FileInfo fileInfo) {
        return fileInfoMapper.insert(FileInfoEntity.from(fileInfo)) > 0;
    }

    @Override
    public void update(FileInfo fileInfo) {
        fileInfoMapper.updateById(FileInfoEntity.from(fileInfo));
    }

    @Override
    public boolean delete(String storageId) {
        // dromara 实际传入的是 fileInfo.getUrl()
        return fileInfoMapper.delete(
                new LambdaQueryWrapper<FileInfoEntity>().eq(FileInfoEntity::getUrl, storageId)) > 0;
    }

    @Override
    public FileInfo getByUrl(String url) {
        FileInfoEntity entity = fileInfoMapper.selectOne(
                new LambdaQueryWrapper<FileInfoEntity>().eq(FileInfoEntity::getUrl, url));
        return entity == null ? null : entity.toFileInfo();
    }

    @Override
    public void saveFilePart(FilePartInfo filePartInfo) {
        // 分片上传当前未使用，留空实现
    }

    @Override
    public void deleteFilePartByUploadId(String uploadId) {
        // 分片上传当前未使用，留空实现
    }
}
