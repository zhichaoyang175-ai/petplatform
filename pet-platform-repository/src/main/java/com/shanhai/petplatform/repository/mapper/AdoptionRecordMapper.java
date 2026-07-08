package com.shanhai.petplatform.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanhai.petplatform.repository.entity.AdoptionRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 领养记录 Mapper
 *
 * @author PetPlatform Team
 */
@Mapper
public interface AdoptionRecordMapper extends BaseMapper<AdoptionRecord> {
}
