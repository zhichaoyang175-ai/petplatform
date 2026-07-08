package com.shanhai.petplatform.api.controller;

import com.shanhai.petplatform.common.dto.response.AdoptionRecordVO;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.common.result.R;
import com.shanhai.petplatform.infrastructure.security.CurrentUser;
import com.shanhai.petplatform.service.AdoptionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/adoptions")
@RequiredArgsConstructor
public class AdoptionRecordController {

    private final AdoptionRecordService recordService;

    @GetMapping("/{id}")
    public R<AdoptionRecordVO> getDetail(@PathVariable Long id) {
        return R.ok(recordService.getAdoptionRecord(id));
    }

    @GetMapping
    public R<PageResult<AdoptionRecordVO>> myAdoptions(@CurrentUser Long userId,
                                                        @RequestParam(required = false) Integer status,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return R.ok(recordService.getMyAdoptions(userId, status, page, size));
    }

    @GetMapping("/my-pets")
    public R<PageResult<AdoptionRecordVO>> mySentPets(@CurrentUser Long userId,
                                                       @RequestParam(required = false) Integer status,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return R.ok(recordService.getMySentPets(userId, status, page, size));
    }
}
