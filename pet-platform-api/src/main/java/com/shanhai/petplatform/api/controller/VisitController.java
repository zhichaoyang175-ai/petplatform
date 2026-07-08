package com.shanhai.petplatform.api.controller;

import com.shanhai.petplatform.common.dto.request.VisitBookRequest;
import com.shanhai.petplatform.common.dto.response.VisitVO;
import com.shanhai.petplatform.common.result.R;
import com.shanhai.petplatform.infrastructure.security.CurrentUser;
import com.shanhai.petplatform.service.VisitApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预约看宠（线下探视）— 申请人发起、送养人确认/完成/取消
 *
 * @author PetPlatform Team
 */
@RestController
@RequestMapping("/api/v1/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitApplicationService visitApplicationService;

    /** 发起预约 — 需登录（申请人） */
    @PostMapping("/book")
    public R<VisitVO> book(@Valid @RequestBody VisitBookRequest request,
                           @CurrentUser Long userId) {
        return R.ok(visitApplicationService.book(request, userId));
    }

    /** 我发起的预约 — 需登录（申请人） */
    @GetMapping("/my")
    public R<List<VisitVO>> my(@CurrentUser Long userId) {
        return R.ok(visitApplicationService.myVisits(userId));
    }

    /** 我收到的预约 — 需登录（送养人） */
    @GetMapping("/received")
    public R<List<VisitVO>> received(@CurrentUser Long userId) {
        return R.ok(visitApplicationService.receivedVisits(userId));
    }

    /** 送养人确认预约 — 需登录（当前 ownerId） */
    @PutMapping("/{id}/confirm")
    public R<VisitVO> confirm(@PathVariable Long id,
                              @CurrentUser Long userId) {
        return R.ok(visitApplicationService.confirm(id, userId));
    }

    /** 完成预约 — 需登录（申请人或送养人） */
    @PutMapping("/{id}/complete")
    public R<VisitVO> complete(@PathVariable Long id,
                               @CurrentUser Long userId) {
        return R.ok(visitApplicationService.complete(id, userId));
    }

    /** 取消预约 — 需登录（申请人或送养人） */
    @PutMapping("/{id}/cancel")
    public R<VisitVO> cancel(@PathVariable Long id,
                             @CurrentUser Long userId) {
        return R.ok(visitApplicationService.cancel(id, userId));
    }

}
