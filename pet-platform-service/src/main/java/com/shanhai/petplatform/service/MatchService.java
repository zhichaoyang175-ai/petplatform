package com.shanhai.petplatform.service;

import com.shanhai.petplatform.common.dto.response.PetVO;
import com.shanhai.petplatform.common.result.PageResult;

/**
 * 智能匹配 / 为你推荐 服务接口
 *
 * <p>基于用户领养画像（住房类型、养宠经验、家人态度）对待领养宠物做轻量、可解释的打分排序，
 * 不引入机器学习。未登录或查不到画像时返回按创建时间倒序的默认推荐。
 *
 * @author PetPlatform Team
 */
public interface MatchService {

    /**
     * 为你推荐
     *
     * @param userId 当前用户ID（可空；为 null 时返回默认推荐）
     * @param page   页码，从 1 开始
     * @param size   每页大小
     * @return 推荐宠物分页结果
     */
    PageResult<PetVO> recommend(Long userId, int page, int size);

}
