package com.shanhai.petplatform.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanhai.petplatform.repository.entity.PetImage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物图片 Mapper
 *
 * @author PetPlatform Team
 */
@Mapper
public interface PetImageMapper extends BaseMapper<PetImage> {
}
