package com.shanhai.petplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanhai.petplatform.common.dto.response.PetVO;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.repository.entity.AdoptionApplication;
import com.shanhai.petplatform.repository.entity.Pet;
import com.shanhai.petplatform.repository.entity.PetImage;
import com.shanhai.petplatform.repository.mapper.AdoptionApplicationMapper;
import com.shanhai.petplatform.repository.mapper.PetImageMapper;
import com.shanhai.petplatform.repository.mapper.PetMapper;
import com.shanhai.petplatform.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能匹配 / 为你推荐 服务实现
 *
 * <p>打分规则（简单可解释，总分越高越优先）：
 * <ul>
 *   <li>有封面图：+15（有图更易促成领养）</li>
 *   <li>已绝育：+12（对租房 / 家庭更友好，始终加分）</li>
 *   <li>健康（healthStatus=1）：+8</li>
 *   <li>养宠经验为「无」：偏好幼年(≤12月)+10，成年(>36月)-6</li>
 *   <li>养宠经验为「正在养」：偏好成年(>12月)+6</li>
 *   <li>住房为「租房」：已绝育额外 +8</li>
 *   <li>住房为「与家人同住」：偏好幼年(≤24月)+8，健康额外 +4</li>
 *   <li>家人态度非「支持」：已绝育额外 +6、健康额外 +4（降低顾虑）</li>
 * </ul>
 *
 * @author PetPlatform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final PetMapper petMapper;
    private final AdoptionApplicationMapper adoptionApplicationMapper;
    private final PetImageMapper petImageMapper;

    /** 待领养状态 */
    private static final int PENDING_STATUS = 0;

    @Override
    public PageResult<PetVO> recommend(Long userId, int page, int size) {
        // 查询本页待领养宠物候选（默认按创建时间倒序）
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<Pet>()
                .eq(Pet::getStatus, PENDING_STATUS)
                .orderByDesc(Pet::getCreatedAt);
        Page<Pet> petPage = petMapper.selectPage(new Page<>(page, size), wrapper);
        List<Pet> pets = petPage.getRecords();
        if (pets.isEmpty()) {
            return PageResult.empty(page, size);
        }

        // 批量查询封面图
        List<Long> petIds = pets.stream().map(Pet::getId).toList();
        Map<Long, String> coverMap = buildCoverMap(petIds);

        // 解析用户偏好画像；为 null 时走默认推荐（按创建时间倒序）
        UserPreference pref = resolvePreference(userId);
        if (pref == null) {
            return toPageResult(pets, coverMap, petPage.getTotal(), page, size);
        }

        // 打分并按分数降序排序（分数相同则用创建时间倒序，保证稳定）
        List<PetVO> voList = pets.stream()
                .map(pet -> {
                    boolean hasImage = coverMap.containsKey(pet.getId());
                    int score = score(pet, pref, hasImage);
                    return new AbstractMap.SimpleEntry<>(pet, score);
                })
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.getValue(), a.getValue());
                    if (cmp != 0) return cmp;
                    LocalDateTime ta = a.getKey().getCreatedAt();
                    LocalDateTime tb = b.getKey().getCreatedAt();
                    if (ta == null || tb == null) return 0;
                    return tb.compareTo(ta);
                })
                .map(e -> toVO(e.getKey(), coverMap.get(e.getKey().getId())))
                .toList();

        return PageResult.of(voList, petPage.getTotal(), page, size);
    }

    // ────────────────── 偏好解析 ──────────────────

    /**
     * 解析用户偏好画像。
     * 读取该用户最近一条领养申请，取 housingType / petExperience / familyAttitude。
     * userId 为 null 或查不到申请时返回 null（调用方走默认推荐）。
     */
    private UserPreference resolvePreference(Long userId) {
        if (userId == null) return null;

        AdoptionApplication latest = adoptionApplicationMapper.selectOne(
                new LambdaQueryWrapper<AdoptionApplication>()
                        .eq(AdoptionApplication::getApplicantId, userId)
                        .orderByDesc(AdoptionApplication::getCreatedAt)
                        .last("LIMIT 1"));

        if (latest == null) {
            log.debug("用户无领养申请画像，使用默认推荐: userId={}", userId);
            return null;
        }
        return new UserPreference(
                latest.getHousingType(),
                latest.getPetExperience(),
                latest.getFamilyAttitude());
    }

    // ────────────────── 打分 ──────────────────

    private int score(Pet pet, UserPreference pref, boolean hasImage) {
        int score = 0;

        if (hasImage) score += 15;
        if (isYes(pet.getNeutered())) score += 12;
        if (pet.getHealthStatus() != null && pet.getHealthStatus() == 1) score += 8;

        Integer age = pet.getAgeMonths();
        Integer experience = pref.petExperience();
        Integer housing = pref.housingType();

        // 养宠经验
        if (experience != null) {
            if (experience == 0) { // 无经验：偏好幼年
                if (age != null && age <= 12) score += 10;
                if (age != null && age > 36) score -= 6;
            } else if (experience == 2) { // 正在养：偏好成年已社会化
                if (age != null && age > 12) score += 6;
            }
        }

        // 住房类型
        if (housing != null) {
            if (housing == 2) { // 租房：更看重绝育
                if (isYes(pet.getNeutered())) score += 8;
            } else if (housing == 3) { // 与家人同住：偏好幼年、健康
                if (age != null && age <= 24) score += 8;
                if (pet.getHealthStatus() != null && pet.getHealthStatus() == 1) score += 4;
            }
        }

        // 家人态度非「支持」：更推荐绝育、健康宠物以降低顾虑
        String attitude = pref.familyAttitude();
        if (attitude != null && !attitude.isBlank()
                && !"支持".equals(attitude) && !"支持领养".equals(attitude)) {
            if (isYes(pet.getNeutered())) score += 6;
            if (pet.getHealthStatus() != null && pet.getHealthStatus() == 1) score += 4;
        }

        return score;
    }

    private boolean isYes(Integer val) {
        return val != null && val == 1;
    }

    // ────────────────── 工具 ──────────────────

    /** 批量查询宠物封面图 */
    private Map<Long, String> buildCoverMap(List<Long> petIds) {
        if (petIds.isEmpty()) return Collections.emptyMap();
        List<PetImage> covers = petImageMapper.selectList(
                new LambdaQueryWrapper<PetImage>()
                        .in(PetImage::getPetId, petIds)
                        .eq(PetImage::getImageType, 1)
                        .eq(PetImage::getSortOrder, 0));
        return covers.stream()
                .collect(Collectors.toMap(PetImage::getPetId, PetImage::getImageUrl, (a, b) -> a));
    }

    private PageResult<PetVO> toPageResult(List<Pet> pets, Map<Long, String> coverMap,
                                           long total, int page, int size) {
        List<PetVO> voList = pets.stream()
                .map(pet -> toVO(pet, coverMap.get(pet.getId())))
                .toList();
        return PageResult.of(voList, total, page, size);
    }

    private PetVO toVO(Pet pet, String coverImage) {
        return PetVO.of(
                pet.getId(), pet.getName(), pet.getBreed(),
                pet.getGender(), pet.getAgeMonths(), pet.getWeightKg(),
                pet.getNeutered(), pet.getHealthStatus(),
                pet.getLocationProvince(), pet.getLocationCity(),
                pet.getDescription(), pet.getStatus(), pet.getViewCount(),
                coverImage, pet.getCreatedAt());
    }

    /** 用户偏好画像（领养申请中提取） */
    private record UserPreference(Integer housingType, Integer petExperience, String familyAttitude)
            implements Serializable {
    }

}
