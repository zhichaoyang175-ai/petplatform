package com.shanhai.petplatform.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanhai.petplatform.repository.entity.AdoptionApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 领养申请 Mapper
 *
 * @author PetPlatform Team
 */
@Mapper
public interface AdoptionApplicationMapper extends BaseMapper<AdoptionApplication> {
}
