package com.shanhai.petplatform.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanhai.petplatform.repository.entity.FollowUpTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 回访任务 Mapper
 *
 * @author PetPlatform Team
 */
@Mapper
public interface FollowUpTaskMapper extends BaseMapper<FollowUpTask> {
}
