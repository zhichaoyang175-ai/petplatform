package com.shanhai.petplatform.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanhai.petplatform.repository.entity.ReviewTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核任务 Mapper
 *
 * @author PetPlatform Team
 */
@Mapper
public interface ReviewTaskMapper extends BaseMapper<ReviewTask> {
}
