package com.shanhai.petplatform.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanhai.petplatform.common.dto.request.PetCreateRequest;
import com.shanhai.petplatform.common.dto.request.PetSearchRequest;
import com.shanhai.petplatform.common.dto.request.PetUpdateRequest;
import com.shanhai.petplatform.common.dto.response.PetDetailVO;
import com.shanhai.petplatform.common.dto.response.PetImageVO;
import com.shanhai.petplatform.common.dto.response.PetVO;
import com.shanhai.petplatform.common.dto.response.StatsVO;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.common.result.R;
import com.shanhai.petplatform.infrastructure.security.CurrentUser;
import com.shanhai.petplatform.repository.entity.AdoptionRecord;
import com.shanhai.petplatform.repository.entity.Pet;
import com.shanhai.petplatform.repository.entity.User;
import com.shanhai.petplatform.repository.mapper.AdoptionRecordMapper;
import com.shanhai.petplatform.repository.mapper.PetMapper;
import com.shanhai.petplatform.repository.mapper.UserMapper;
import com.shanhai.petplatform.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 宠物管理 — CRUD + 图片 + 统计
 *
 * @author PetPlatform Team
 */
@RestController
@RequestMapping("/api/v1/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;
    private final PetMapper petMapper;
    private final AdoptionRecordMapper adoptionRecordMapper;
    private final UserMapper userMapper;

    // ────────────────── 宠物 CRUD ──────────────────

    /** 分页搜索宠物 — 无需登录 */
    @GetMapping
    public R<PageResult<PetVO>> searchPets(PetSearchRequest request) {
        return R.ok(petService.searchPets(request));
    }

    /** 获取宠物详情 — currentUserId 可选 */
    @GetMapping("/{id}")
    public R<PetDetailVO> getDetail(@PathVariable Long id,
                                     @RequestParam(required = false) Long currentUserId) {
        return R.ok(petService.getPetDetail(id, currentUserId));
    }

    /** 发布宠物 — 需登录 */
    @PostMapping
    public R<PetVO> create(@Valid @RequestBody PetCreateRequest request,
                            @CurrentUser Long userId) {
        return R.ok(petService.createPet(request, userId));
    }

    /** 更新宠物 — 需登录 */
    @PutMapping("/{id}")
    public R<PetVO> update(@PathVariable Long id,
                            @Valid @RequestBody PetUpdateRequest request,
                            @CurrentUser Long userId) {
        return R.ok(petService.updatePet(id, request, userId));
    }

    /** 下架宠物 — 需登录 */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id, @CurrentUser Long userId) {
        petService.deletePet(id, userId);
        return R.ok();
    }

    /** 修改宠物状态 — 需登录 */
    @PutMapping("/{id}/status")
    public R<PetVO> updateStatus(@PathVariable Long id,
                                  @RequestParam Integer status,
                                  @CurrentUser Long userId) {
        return R.ok(petService.updatePetStatus(id, status, userId));
    }

    /** 上传宠物图片 — 需登录 */
    @PostMapping("/{id}/images")
    public R<List<PetImageVO>> uploadImages(@PathVariable Long id,
                                             @RequestParam("files") MultipartFile[] files,
                                             @CurrentUser Long userId) {
        return R.ok(petService.uploadPetImages(id, files, userId));
    }

    /** 删除宠物图片 — 需登录 */
    @DeleteMapping("/{id}/images/{imageId}")
    public R<Void> deleteImage(@PathVariable Long id,
                                @PathVariable Long imageId,
                                @CurrentUser Long userId) {
        petService.deletePetImage(id, imageId, userId);
        return R.ok();
    }

    /** 我的宠物列表 — 需登录 */
    @GetMapping("/my")
    public R<PageResult<PetVO>> getMyPets(@CurrentUser Long userId,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return R.ok(petService.getMyPets(userId, status, page, size));
    }

    // ────────────────── 平台统计 ──────────────────

    /** 平台统计数据 — 无需登录 */
    @GetMapping("/stats")
    public R<StatsVO> getStats() {
        long pendingAdoption = petMapper.selectCount(
                new LambdaQueryWrapper<Pet>().eq(Pet::getStatus, 0));
        long successfulAdoption = adoptionRecordMapper.selectCount(
                new LambdaQueryWrapper<AdoptionRecord>().eq(AdoptionRecord::getStatus, 2));
        long partnerShelters = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, 2).eq(User::getStatus, 1));

        return R.ok(StatsVO.of(pendingAdoption, successfulAdoption, partnerShelters));
    }

}
