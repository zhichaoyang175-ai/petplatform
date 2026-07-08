package com.shanhai.petplatform.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanhai.petplatform.common.dto.request.ApplicationReviewRequest;
import com.shanhai.petplatform.common.dto.request.ApplicationSubmitRequest;
import com.shanhai.petplatform.common.dto.response.ApplicationVO;
import com.shanhai.petplatform.common.result.R;
import com.shanhai.petplatform.infrastructure.security.CurrentUser;
import com.shanhai.petplatform.repository.entity.AdoptionApplication;
import com.shanhai.petplatform.repository.entity.Pet;
import com.shanhai.petplatform.repository.mapper.AdoptionApplicationMapper;
import com.shanhai.petplatform.repository.mapper.PetMapper;
import com.shanhai.petplatform.service.state.AdoptionStateMachine;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class AdoptionApplicationController {

    private final AdoptionStateMachine stateMachine;
    private final AdoptionApplicationMapper appMapper;
    private final PetMapper petMapper;

    @PostMapping
    public R<ApplicationVO> submit(@Valid @RequestBody ApplicationSubmitRequest req, @CurrentUser Long userId) {
        return R.ok(stateMachine.submitApplication(req, userId));
    }

    @GetMapping("/{id}")
    public R<ApplicationVO> getDetail(@PathVariable Long id) {
        AdoptionApplication a = appMapper.selectById(id);
        if (a == null) return R.fail(404, "申请不存在");
        Pet p = petMapper.selectById(a.getPetId());
        return R.ok(ApplicationVO.of(a.getId(), a.getPetId(), p != null ? p.getName() : "未知",
                p != null ? p.getBreed() : "未知", p != null && p.getBreed() != null && p.getBreed().contains("猫") ? "🐱" : "🐕",
                a.getApplicantId(), null, a.getHousingType(), a.getPetExperience(),
                a.getMonthlyIncome(), a.getFamilyAttitude(), a.getCurrentPets(), a.getReason(),
                a.getStatus(), a.getRejectReason(), a.getCreatedAt()));
    }

    @GetMapping
    public R<Object> myApplications(@CurrentUser Long userId,
                                     @RequestParam(required = false) Integer status,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<AdoptionApplication> w = new LambdaQueryWrapper<AdoptionApplication>()
                .eq(AdoptionApplication::getApplicantId, userId);
        if (status != null) w.eq(AdoptionApplication::getStatus, status);
        w.orderByDesc(AdoptionApplication::getCreatedAt);

        Page<AdoptionApplication> p = appMapper.selectPage(new Page<>(page, size), w);
        List<ApplicationVO> vos = p.getRecords().stream().map(a -> {
            Pet pet = petMapper.selectById(a.getPetId());
            return ApplicationVO.of(a.getId(), a.getPetId(), pet != null ? pet.getName() : "未知",
                    pet != null ? pet.getBreed() : "未知", pet != null && pet.getBreed() != null && pet.getBreed().contains("猫") ? "🐱" : "🐕",
                    a.getApplicantId(), null, a.getHousingType(), a.getPetExperience(),
                    a.getMonthlyIncome(), a.getFamilyAttitude(), a.getCurrentPets(), a.getReason(),
                    a.getStatus(), a.getRejectReason(), a.getCreatedAt());
        }).toList();

        return R.ok(new Object() {
            public final List<ApplicationVO> records = vos;
            public final long total = p.getTotal();
            public final int current = (int) p.getCurrent();
            public final int size = (int) p.getSize();
        });
    }

    @GetMapping("/received")
    public R<Object> receivedApplications(@CurrentUser Long userId,
                                           @RequestParam(required = false) Long petId,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        // 查该用户的宠物ID列表
        List<Long> petIds = petMapper.selectList(
                new LambdaQueryWrapper<Pet>().eq(Pet::getOwnerId, userId))
                .stream().map(Pet::getId).toList();
        if (petIds.isEmpty()) {
            return R.ok(new PageResultFallback());
        }

        LambdaQueryWrapper<AdoptionApplication> w = new LambdaQueryWrapper<AdoptionApplication>()
                .in(AdoptionApplication::getPetId, petIds);
        if (petId != null) w.eq(AdoptionApplication::getPetId, petId);
        if (status != null) w.eq(AdoptionApplication::getStatus, status);
        w.orderByDesc(AdoptionApplication::getCreatedAt);

        Page<AdoptionApplication> p = appMapper.selectPage(new Page<>(page, size), w);
        List<ApplicationVO> vos = p.getRecords().stream().map(a -> {
            Pet pet = petMapper.selectById(a.getPetId());
            return ApplicationVO.of(a.getId(), a.getPetId(), pet != null ? pet.getName() : "未知",
                    pet != null ? pet.getBreed() : "未知", pet != null && pet.getBreed() != null && pet.getBreed().contains("猫") ? "🐱" : "🐕",
                    a.getApplicantId(), null, a.getHousingType(), a.getPetExperience(),
                    a.getMonthlyIncome(), a.getFamilyAttitude(), a.getCurrentPets(), a.getReason(),
                    a.getStatus(), a.getRejectReason(), a.getCreatedAt());
        }).toList();

        return R.ok(new Object() {
            public final List<ApplicationVO> records = vos;
            public final long total = p.getTotal();
            public final int current = (int) p.getCurrent();
            public final int size = (int) p.getSize();
        });
    }

    @PutMapping("/{id}/review")
    public R<ApplicationVO> review(@PathVariable Long id,
                                    @Valid @RequestBody ApplicationReviewRequest req,
                                    @CurrentUser Long userId) {
        return R.ok(stateMachine.reviewApplication(id, req, userId));
    }

    @PutMapping("/{id}/cancel")
    public R<ApplicationVO> cancel(@PathVariable Long id, @CurrentUser Long userId) {
        return R.ok(stateMachine.cancelApplication(id, userId));
    }

    private static class PageResultFallback {
        public final List<?> records = List.of();
        public final long total = 0;
        public final int current = 1;
        public final int size = 10;
    }
}
