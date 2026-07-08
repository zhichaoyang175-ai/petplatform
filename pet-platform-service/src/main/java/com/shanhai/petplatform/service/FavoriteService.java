package com.shanhai.petplatform.service;

import com.shanhai.petplatform.common.dto.response.PetVO;
import com.shanhai.petplatform.common.result.PageResult;

/**
 * 收藏服务接口
 */
public interface FavoriteService {

    void addFavorite(Long petId, Long userId);

    void removeFavorite(Long petId, Long userId);

    PageResult<PetVO> getFavorites(Long userId, int page, int size);

    boolean isFavorited(Long petId, Long userId);

}
