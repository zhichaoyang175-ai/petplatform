package com.shanhai.petplatform.api.controller;

import com.shanhai.petplatform.common.dto.response.PetVO;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.common.result.R;
import com.shanhai.petplatform.infrastructure.security.CurrentUser;
import com.shanhai.petplatform.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 智能匹配 / 为你推荐
 *
 * @author PetPlatform Team
 */
@RestController
@RequestMapping("/api/v1/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    /**
     * 为你推荐
     * <p>userId 可选：登录态下由 @CurrentUser 注入；也允许匿名通过 query 参数显式传入 userId 或不传。
     * 未登录且无 userId 时返回按创建时间倒序的默认推荐。
     */
    @GetMapping("/recommendations")
    public R<PageResult<PetVO>> recommendations(
            @CurrentUser Long loginUserId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long effectiveUserId = userId != null ? userId : loginUserId;
        return R.ok(matchService.recommend(effectiveUserId, page, size));
    }

}
