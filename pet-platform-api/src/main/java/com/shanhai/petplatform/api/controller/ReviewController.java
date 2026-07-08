package com.shanhai.petplatform.api.controller;

import com.shanhai.petplatform.common.dto.request.ReviewActionRequest;
import com.shanhai.petplatform.common.dto.response.ReviewTaskVO;
import com.shanhai.petplatform.common.result.R;
import com.shanhai.petplatform.infrastructure.security.CurrentUser;
import com.shanhai.petplatform.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审核中心（审核员 / 管理员）
 *
 * @author PetPlatform Team
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAnyRole('REVIEWER','ADMIN')")
public class ReviewController {

    private final ReviewService reviewService;

    /** 待审核 / 已处理列表（status 不传默认待审核） */
    @GetMapping
    public R<com.shanhai.petplatform.common.result.PageResult<ReviewTaskVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(reviewService.listPending(status, page, size));
    }

    @GetMapping("/{id}")
    public R<ReviewTaskVO> detail(@PathVariable Long id) {
        return R.ok(reviewService.getDetail(id));
    }

    @PutMapping("/{id}/approve")
    public R<ReviewTaskVO> approve(@PathVariable Long id,
                                   @CurrentUser Long userId,
                                   @RequestBody(required = false) ReviewActionRequest req) {
        return R.ok(reviewService.approve(id, userId, req == null ? null : req.getComment()));
    }

    @PutMapping("/{id}/reject")
    public R<ReviewTaskVO> reject(@PathVariable Long id,
                                  @CurrentUser Long userId,
                                  @RequestBody(required = false) ReviewActionRequest req) {
        if (req == null || req.getComment() == null || req.getComment().isBlank())
            return R.fail(400, "请填写驳回原因");
        return R.ok(reviewService.reject(id, userId, req.getComment()));
    }
}
