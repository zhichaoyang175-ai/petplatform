package com.shanhai.petplatform.api.controller;

import com.shanhai.petplatform.common.dto.request.FollowUpSubmitRequest;
import com.shanhai.petplatform.common.dto.response.FollowUpRecordVO;
import com.shanhai.petplatform.common.dto.response.FollowUpTaskVO;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.common.result.R;
import com.shanhai.petplatform.infrastructure.security.CurrentUser;
import com.shanhai.petplatform.service.FollowUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/follow-ups")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;

    @GetMapping("/tasks")
    public R<PageResult<FollowUpTaskVO>> tasks(@CurrentUser Long userId,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return R.ok(followUpService.getMyTasks(userId, status, page, size));
    }

    @GetMapping("/tasks/{id}")
    public R<FollowUpTaskVO> taskDetail(@PathVariable Long id, @CurrentUser Long userId) {
        return R.ok(followUpService.getTaskDetail(id, userId));
    }

    @PostMapping("/tasks/{id}/submit")
    public R<FollowUpRecordVO> submit(@PathVariable Long id,
                                       @Valid @RequestBody FollowUpSubmitRequest req,
                                       @CurrentUser Long userId) {
        return R.ok(followUpService.submitFollowUp(id, req, userId));
    }

    @GetMapping("/records")
    public R<PageResult<FollowUpRecordVO>> records(@RequestParam Long adoptionRecordId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return R.ok(followUpService.getRecords(adoptionRecordId, page, size));
    }
}
