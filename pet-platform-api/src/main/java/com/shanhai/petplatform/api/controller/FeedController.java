package com.shanhai.petplatform.api.controller;

import com.shanhai.petplatform.common.dto.request.FeedRequest;
import com.shanhai.petplatform.common.dto.response.FeedVO;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.common.result.R;
import com.shanhai.petplatform.infrastructure.security.CurrentUser;
import com.shanhai.petplatform.service.FeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 领养动态/领养故事 — 社区模块
 *
 * @author PetPlatform Team
 */
@RestController
@RequestMapping("/api/v1/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    /** 公开动态列表（分页） */
    @GetMapping
    public R<PageResult<FeedVO>> listFeeds(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return R.ok(feedService.listFeeds(page, size));
    }

    /** 动态详情 */
    @GetMapping("/{id}")
    public R<FeedVO> getFeed(@PathVariable Long id) {
        return R.ok(feedService.getFeed(id));
    }

    /** 发布动态 — 需登录 */
    @PostMapping
    public R<FeedVO> create(@Valid @RequestBody FeedRequest request,
                            @CurrentUser Long userId) {
        return R.ok(feedService.createFeed(request, userId));
    }

    /** 点赞 — 简单计数 +1 */
    @PostMapping("/{id}/like")
    public R<Void> like(@PathVariable Long id) {
        feedService.likeFeed(id);
        return R.ok();
    }

    /** 我的动态 — 需登录 */
    @GetMapping("/my")
    public R<PageResult<FeedVO>> getMyFeeds(@CurrentUser Long userId,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return R.ok(feedService.getMyFeeds(userId, page, size));
    }

}
