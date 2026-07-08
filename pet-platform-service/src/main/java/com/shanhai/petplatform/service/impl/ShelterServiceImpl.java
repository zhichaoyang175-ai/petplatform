package com.shanhai.petplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanhai.petplatform.common.dto.request.ShelterRequest;
import com.shanhai.petplatform.common.dto.response.PetVO;
import com.shanhai.petplatform.common.dto.response.ShelterVO;
import com.shanhai.petplatform.common.enums.UserRoleEnum;
import com.shanhai.petplatform.common.exception.ForbiddenException;
import com.shanhai.petplatform.common.exception.NotFoundException;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.repository.entity.Pet;
import com.shanhai.petplatform.repository.entity.PetImage;
import com.shanhai.petplatform.repository.entity.Shelter;
import com.shanhai.petplatform.repository.entity.User;
import com.shanhai.petplatform.repository.mapper.PetImageMapper;
import com.shanhai.petplatform.repository.mapper.PetMapper;
import com.shanhai.petplatform.repository.mapper.ShelterMapper;
import com.shanhai.petplatform.repository.mapper.UserMapper;
import com.shanhai.petplatform.service.ShelterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 救助站服务实现
 *
 * @author PetPlatform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShelterServiceImpl implements ShelterService {

    private final ShelterMapper shelterMapper;
    private final PetMapper petMapper;
    private final PetImageMapper petImageMapper;
    private final UserMapper userMapper;

    /** 救助站默认状态：0-待审核 */
    private static final int DEFAULT_STATUS = 0;

    // ────────────────── 公开列表 ──────────────────

    @Override
    public PageResult<ShelterVO> listShelters(String province, String city, String keyword, int page, int size) {
        LambdaQueryWrapper<Shelter> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(province)) wrapper.eq(Shelter::getProvince, province);
        if (StringUtils.hasText(city)) wrapper.eq(Shelter::getCity, city);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Shelter::getName, keyword)
                    .or().like(Shelter::getDescription, keyword)
                    .or().like(Shelter::getAddress, keyword));
        }
        wrapper.orderByDesc(Shelter::getCreatedAt);

        Page<Shelter> shelterPage = shelterMapper.selectPage(new Page<>(page, size), wrapper);
        List<ShelterVO> voList = shelterPage.getRecords().stream()
                .map(this::toVO)
                .toList();

        return PageResult.of(voList, shelterPage.getTotal(),
                (int) shelterPage.getCurrent(), (int) shelterPage.getSize());
    }

    // ────────────────── 公开详情 ──────────────────

    @Override
    public ShelterVO getShelter(Long shelterId) {
        Shelter shelter = shelterMapper.selectById(shelterId);
        if (shelter == null) throw new NotFoundException("救助站不存在");
        return toVO(shelter);
    }

    // ────────────────── 在养宠物 ──────────────────

    @Override
    public List<PetVO> getShelterPets(Long shelterId) {
        Shelter shelter = shelterMapper.selectById(shelterId);
        if (shelter == null) throw new NotFoundException("救助站不存在");

        List<Pet> pets = petMapper.selectList(
                new LambdaQueryWrapper<Pet>()
                        .eq(Pet::getOwnerId, shelter.getOwnerUserId())
                        .orderByDesc(Pet::getCreatedAt));

        List<Long> petIds = pets.stream().map(Pet::getId).toList();
        Map<Long, String> coverMap = buildCoverMap(petIds);

        return pets.stream()
                .map(p -> PetVO.of(
                        p.getId(), p.getName(), p.getBreed(),
                        p.getGender(), p.getAgeMonths(), p.getWeightKg(),
                        p.getNeutered(), p.getHealthStatus(),
                        p.getLocationProvince(), p.getLocationCity(),
                        p.getDescription(), p.getStatus(), p.getViewCount(),
                        coverMap.get(p.getId()), p.getCreatedAt()))
                .toList();
    }

    // ────────────────── 创建 ──────────────────

    @Override
    public ShelterVO createShelter(ShelterRequest request, Long userId) {
        if (userId == null) throw new ForbiddenException("请先登录后再创建救助站");

        Shelter shelter = new Shelter();
        shelter.setOwnerUserId(userId);
        shelter.setName(request.getName());
        shelter.setDescription(request.getDescription());
        shelter.setAddress(request.getAddress());
        shelter.setProvince(request.getProvince());
        shelter.setCity(request.getCity());
        shelter.setContactPhone(request.getContactPhone());
        shelter.setLogoUrl(request.getLogoUrl());
        shelter.setStatus(DEFAULT_STATUS);

        shelterMapper.insert(shelter);
        log.info("创建救助站: shelterId={}, ownerUserId={}", shelter.getId(), userId);

        return toVO(shelter);
    }

    // ────────────────── 更新 ──────────────────

    @Override
    public ShelterVO updateShelter(Long shelterId, ShelterRequest request, Long userId) {
        Shelter shelter = shelterMapper.selectById(shelterId);
        if (shelter == null) throw new NotFoundException("救助站不存在");
        if (!isOwnerOrAdmin(shelter, userId)) throw new ForbiddenException("只能修改自己的救助站");

        if (request.getName() != null) shelter.setName(request.getName());
        if (request.getDescription() != null) shelter.setDescription(request.getDescription());
        if (request.getAddress() != null) shelter.setAddress(request.getAddress());
        if (request.getProvince() != null) shelter.setProvince(request.getProvince());
        if (request.getCity() != null) shelter.setCity(request.getCity());
        if (request.getContactPhone() != null) shelter.setContactPhone(request.getContactPhone());
        if (request.getLogoUrl() != null) shelter.setLogoUrl(request.getLogoUrl());

        shelterMapper.updateById(shelter);
        return toVO(shelter);
    }

    // ────────────────── 我的救助站 ──────────────────

    @Override
    public List<ShelterVO> getMyShelters(Long userId) {
        if (userId == null) throw new ForbiddenException("请先登录");
        List<Shelter> list = shelterMapper.selectList(
                new LambdaQueryWrapper<Shelter>()
                        .eq(Shelter::getOwnerUserId, userId)
                        .orderByDesc(Shelter::getCreatedAt));
        return list.stream().map(this::toVO).toList();
    }

    // ────────────────── 工具 ──────────────────

    private ShelterVO toVO(Shelter shelter) {
        return ShelterVO.of(
                shelter.getId(), shelter.getOwnerUserId(), shelter.getName(),
                shelter.getDescription(), shelter.getAddress(), shelter.getProvince(),
                shelter.getCity(), shelter.getContactPhone(), shelter.getLogoUrl(),
                shelter.getStatus(), shelter.getCreatedAt(), shelter.getUpdatedAt());
    }

    /** 创建者本人或管理员可操作 */
    private boolean isOwnerOrAdmin(Shelter shelter, Long userId) {
        if (userId == null) return false;
        if (shelter.getOwnerUserId() != null && shelter.getOwnerUserId().equals(userId)) return true;
        User user = userMapper.selectById(userId);
        return user != null && user.getRole() != null
                && user.getRole() == UserRoleEnum.ADMIN.getCode();
    }

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

}
