package com.shanhai.petplatform.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanhai.petplatform.infrastructure.storage.FileInfoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件上传记录 Mapper
 *
 * @author PetPlatform Team
 */
@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfoEntity> {
}
