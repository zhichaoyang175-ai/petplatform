package com.shanhai.petplatform.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanhai.petplatform.repository.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏 Mapper
 *
 * @author PetPlatform Team
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
