package com.shanhai.petplatform.api.controller;

import com.shanhai.petplatform.common.dto.request.ShelterRequest;
import com.shanhai.petplatform.common.dto.response.PetVO;
import com.shanhai.petplatform.common.dto.response.ShelterVO;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.common.result.R;
import com.shanhai.petplatform.infrastructure.security.CurrentUser;
import com.shanhai.petplatform.service.ShelterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 救助站管理 — 公开列表/详情 + 登录创建/更新/我的
 *
 * @author PetPlatform Team
 */
@RestController
@RequestMapping("/api/v1/shelters")
@RequiredArgsConstructor
public class ShelterController {

    private final ShelterService shelterService;

    /** 公开列表（分页，支持 province/city/keyword 过滤） */
    @GetMapping
    public R<PageResult<ShelterVO>> listShelters(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return R.ok(shelterService.listShelters(province, city, keyword, page, size));
    }

    /** 公开详情 */
    @GetMapping("/{id}")
    public R<ShelterVO> getShelter(@PathVariable Long id) {
        return R.ok(shelterService.getShelter(id));
    }

    /** 该救助站在养宠物列表 */
    @GetMapping("/{id}/pets")
    public R<List<PetVO>> getShelterPets(@PathVariable Long id) {
        return R.ok(shelterService.getShelterPets(id));
    }

    /** 创建救助站 — 需登录 */
    @PostMapping
    public R<ShelterVO> create(@Valid @RequestBody ShelterRequest request,
                               @CurrentUser Long userId) {
        return R.ok(shelterService.createShelter(request, userId));
    }

    /** 更新救助站 — 需登录（仅创建者或管理员） */
    @PutMapping("/{id}")
    public R<ShelterVO> update(@PathVariable Long id,
                               @Valid @RequestBody ShelterRequest request,
                               @CurrentUser Long userId) {
        return R.ok(shelterService.updateShelter(id, request, userId));
    }

    /** 我的救助站 — 需登录 */
    @GetMapping("/my")
    public R<List<ShelterVO>> getMy(@CurrentUser Long userId) {
        return R.ok(shelterService.getMyShelters(userId));
    }

}
