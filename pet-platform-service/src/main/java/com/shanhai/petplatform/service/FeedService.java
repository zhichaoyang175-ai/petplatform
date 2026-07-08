package com.shanhai.petplatform.service;

import com.shanhai.petplatform.common.dto.request.FeedRequest;
import com.shanhai.petplatform.common.dto.response.FeedVO;
import com.shanhai.petplatform.common.result.PageResult;

/**
 * 领养动态服务接口
 *
 * @author PetPlatform Team
 */
public interface FeedService {

    /** 分页获取公开动态列表 */
    PageResult<FeedVO> listFeeds(int page, int size);

    /** 获取单条动态详情 */
    FeedVO getFeed(Long feedId);

    /** 发布动态 — 需登录 */
    FeedVO createFeed(FeedRequest request, Long authorId);

    /** 点赞 — 简单计数 +1 */
    void likeFeed(Long feedId);

    /** 分页获取我的动态 */
    PageResult<FeedVO> getMyFeeds(Long authorId, int page, int size);

}
