package com.shanhai.petplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanhai.petplatform.common.constant.RedisKeyConstant;
import com.shanhai.petplatform.common.dto.request.PetCreateRequest;
import com.shanhai.petplatform.common.dto.request.PetSearchRequest;
import com.shanhai.petplatform.common.dto.request.PetUpdateRequest;
import com.shanhai.petplatform.common.dto.response.*;
import com.shanhai.petplatform.common.exception.BusinessException;
import com.shanhai.petplatform.common.exception.ForbiddenException;
import com.shanhai.petplatform.common.exception.NotFoundException;
import com.shanhai.petplatform.common.constant.RedisKeyConstant;
import com.shanhai.petplatform.common.dto.request.PetCreateRequest;
import com.shanhai.petplatform.common.dto.request.PetSearchRequest;
import com.shanhai.petplatform.common.dto.request.PetUpdateRequest;
import com.shanhai.petplatform.common.dto.response.*;
import com.shanhai.petplatform.common.exception.BusinessException;
import com.shanhai.petplatform.common.exception.ForbiddenException;
import com.shanhai.petplatform.common.exception.NotFoundException;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.infrastructure.cache.CacheService;
import com.shanhai.petplatform.infrastructure.storage.FileStorageStrategy;
import com.shanhai.petplatform.repository.entity.*;
import com.shanhai.petplatform.repository.mapper.*;
import com.shanhai.petplatform.service.PetService;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 宠物服务实现
 *
 * @author PetPlatform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetMapper petMapper;
    private final PetImageMapper petImageMapper;
    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;
    private final FileStorageStrategy fileStorageStrategy;
    private final CacheService cacheService;

    // Redisson 为可选依赖：本地未启动 Redis 时 RedissonConfig 返回 null，此处为 null，
    // getHotPets 自动降级为无锁直查 DB（与 CacheService 的 Redis 静默降级策略一致）。
    @Autowired(required = false)
    private RedissonClient redissonClient;

    private static final int OFFLINE_STATUS = 6;

    // ────────────────── 搜索 ──────────────────

    @Override
    public PageResult<PetVO> searchPets(PetSearchRequest request) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();

        // 默认排除已下架
        wrapper.ne(Pet::getStatus, OFFLINE_STATUS);

        if (request.getBreed() != null && !request.getBreed().isBlank())
            wrapper.eq(Pet::getBreed, request.getBreed());
        if (request.getAgeMin() != null)
            wrapper.ge(Pet::getAgeMonths, request.getAgeMin());
        if (request.getAgeMax() != null)
            wrapper.le(Pet::getAgeMonths, request.getAgeMax());
        if (request.getGender() != null)
            wrapper.eq(Pet::getGender, request.getGender());
        if (request.getNeutered() != null)
            wrapper.eq(Pet::getNeutered, request.getNeutered());
        if (request.getHealthStatus() != null)
            wrapper.eq(Pet::getHealthStatus, request.getHealthStatus());
        if (request.getProvince() != null && !request.getProvince().isBlank())
            wrapper.eq(Pet::getLocationProvince, request.getProvince());
        if (request.getCity() != null && !request.getCity().isBlank())
            wrapper.eq(Pet::getLocationCity, request.getCity());
        if (request.getStatus() != null)
            wrapper.eq(Pet::getStatus, request.getStatus());

        // 排序
        if ("view_count".equals(request.getSortBy())) {
            wrapper.orderByDesc(Pet::getViewCount);
        } else {
            wrapper.orderByDesc(Pet::getCreatedAt);
        }

        Page<Pet> page = new Page<>(request.getPage(), request.getSize());
        Page<Pet> petPage = petMapper.selectPage(page, wrapper);

        // 批量查询封面图
        List<Long> petIds = petPage.getRecords().stream().map(Pet::getId).toList();
        Map<Long, String> coverMap = buildCoverMap(petIds);

        // 转换为 PetVO
        List<PetVO> voList = petPage.getRecords().stream()
                .map(pet -> PetVO.of(
                        pet.getId(), pet.getName(), pet.getBreed(),
                        pet.getGender(), pet.getAgeMonths(), pet.getWeightKg(),
                        pet.getNeutered(), pet.getHealthStatus(),
                        pet.getLocationProvince(), pet.getLocationCity(),
                        pet.getDescription(), pet.getStatus(), pet.getViewCount(),
                        coverMap.get(pet.getId()), pet.getCreatedAt()))
                .toList();

        return PageResult.of(voList, petPage.getTotal(), (int) petPage.getCurrent(), (int) petPage.getSize());
    }

    // ────────────────── 详情 ──────────────────

    @Override
    public PetDetailVO getPetDetail(Long petId, Long currentUserId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            throw new NotFoundException("宠物不存在");
        }

        // 浏览计数（Redis，达到阈值批量写回 DB）
        long views = cacheService.increment(RedisKeyConstant.petViewKey(petId), 1);
        if (views % 10 == 0) {
            pet.setViewCount((int) views);
            petMapper.updateById(pet);
        }

        // 基础 VO
        PetVO petVO = PetVO.of(
                pet.getId(), pet.getName(), pet.getBreed(),
                pet.getGender(), pet.getAgeMonths(), pet.getWeightKg(),
                pet.getNeutered(), pet.getHealthStatus(),
                pet.getLocationProvince(), pet.getLocationCity(),
                pet.getDescription(), pet.getStatus(), pet.getViewCount(),
                null, pet.getCreatedAt());

        // 图片列表
        List<PetImage> images = petImageMapper.selectList(
                new LambdaQueryWrapper<PetImage>()
                        .eq(PetImage::getPetId, petId)
                        .orderByAsc(PetImage::getSortOrder));
        List<PetImageVO> imageVOs = images.stream()
                .map(img -> PetImageVO.of(img.getId(), img.getImageUrl(), img.getImageType(), img.getSortOrder()))
                .toList();
        String coverImage = imageVOs.isEmpty() ? null : imageVOs.get(0).getImageUrl();
        petVO.setCoverImage(coverImage);

        // 送养人信息
        User owner = userMapper.selectById(pet.getOwnerId());
        UserVO ownerVO = owner != null ? UserVO.of(
                owner.getId(), maskPhone(owner.getPhone()), owner.getNickname(),
                owner.getAvatarUrl(), owner.getEmail(), owner.getRole(), owner.getCreatedAt()) : null;

        // 是否已收藏
        boolean isFavorited = false;
        if (currentUserId != null) {
            isFavorited = favoriteMapper.selectCount(
                    new LambdaQueryWrapper<Favorite>()
                            .eq(Favorite::getUserId, currentUserId)
                            .eq(Favorite::getPetId, petId)) > 0;
        }

        // 相似宠物推荐
        List<Pet> similar = petMapper.selectList(
                new LambdaQueryWrapper<Pet>()
                        .eq(Pet::getBreed, pet.getBreed())
                        .eq(Pet::getStatus, 0)
                        .ne(Pet::getId, petId)
                        .last("LIMIT 4"));
        List<PetVO> similarVOs = similar.stream()
                .map(p -> PetVO.of(p.getId(), p.getName(), p.getBreed(),
                        p.getGender(), p.getAgeMonths(), p.getWeightKg(),
                        p.getNeutered(), p.getHealthStatus(),
                        p.getLocationProvince(), p.getLocationCity(),
                        p.getDescription(), p.getStatus(), p.getViewCount(),
                        null, p.getCreatedAt()))
                .toList();

        return PetDetailVO.fromPetVO(petVO, pet.getAdoptionRequirements(),
                imageVOs, ownerVO, isFavorited, similarVOs);
    }

    // ────────────────── 发布 ──────────────────

    @Override
    @Transactional
    public PetVO createPet(PetCreateRequest request, Long ownerId) {
        // 仅送养人（role=2）可发布领养信息，防止越权发布
        User owner = userMapper.selectById(ownerId);
        if (owner == null || owner.getRole() == null || owner.getRole() != 2) {
            throw new ForbiddenException("仅送养人可以发布领养信息");
        }
        Pet pet = new Pet();
        pet.setOwnerId(ownerId);
        pet.setName(request.getName());
        pet.setBreed(request.getBreed());
        pet.setGender(request.getGender());
        pet.setAgeMonths(request.getAgeMonths());
        pet.setWeightKg(request.getWeightKg());
        pet.setNeutered(request.getNeutered() != null ? request.getNeutered() : 0);
        pet.setHealthStatus(request.getHealthStatus() != null ? request.getHealthStatus() : 1);
        pet.setLocationProvince(request.getLocationProvince());
        pet.setLocationCity(request.getLocationCity());
        pet.setDescription(request.getDescription());
        pet.setAdoptionRequirements(request.getAdoptionRequirements());
        pet.setStatus(0); // 待领养
        pet.setViewCount(0);

        petMapper.insert(pet);
        log.info("宠物发布: petId={}, ownerId={}", pet.getId(), ownerId);

        return PetVO.of(pet.getId(), pet.getName(), pet.getBreed(),
                pet.getGender(), pet.getAgeMonths(), pet.getWeightKg(),
                pet.getNeutered(), pet.getHealthStatus(),
                pet.getLocationProvince(), pet.getLocationCity(),
                pet.getDescription(), pet.getStatus(), 0, null, pet.getCreatedAt());
    }

    // ────────────────── 更新 ──────────────────

    @Override
    @Transactional
    public PetVO updatePet(Long petId, PetUpdateRequest request, Long ownerId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) throw new NotFoundException("宠物不存在");
        if (!pet.getOwnerId().equals(ownerId)) throw new ForbiddenException("只能编辑自己的宠物");

        if (request.getName() != null) pet.setName(request.getName());
        if (request.getBreed() != null) pet.setBreed(request.getBreed());
        if (request.getGender() != null) pet.setGender(request.getGender());
        if (request.getAgeMonths() != null) pet.setAgeMonths(request.getAgeMonths());
        if (request.getWeightKg() != null) pet.setWeightKg(request.getWeightKg());
        if (request.getNeutered() != null) pet.setNeutered(request.getNeutered());
        if (request.getHealthStatus() != null) pet.setHealthStatus(request.getHealthStatus());
        if (request.getLocationProvince() != null) pet.setLocationProvince(request.getLocationProvince());
        if (request.getLocationCity() != null) pet.setLocationCity(request.getLocationCity());
        if (request.getDescription() != null) pet.setDescription(request.getDescription());
        if (request.getAdoptionRequirements() != null) pet.setAdoptionRequirements(request.getAdoptionRequirements());

        petMapper.updateById(pet);

        return PetVO.of(pet.getId(), pet.getName(), pet.getBreed(),
                pet.getGender(), pet.getAgeMonths(), pet.getWeightKg(),
                pet.getNeutered(), pet.getHealthStatus(),
                pet.getLocationProvince(), pet.getLocationCity(),
                pet.getDescription(), pet.getStatus(), pet.getViewCount(),
                null, pet.getCreatedAt());
    }

    // ────────────────── 删除（软删除） ──────────────────

    @Override
    public void deletePet(Long petId, Long ownerId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) throw new NotFoundException("宠物不存在");
        if (!pet.getOwnerId().equals(ownerId)) throw new ForbiddenException("只能删除自己的宠物");

        pet.setStatus(OFFLINE_STATUS);
        petMapper.updateById(pet);
        log.info("宠物下架: petId={}", petId);
    }

    // ────────────────── 状态修改 ──────────────────

    @Override
    public PetVO updatePetStatus(Long petId, Integer status, Long ownerId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) throw new NotFoundException("宠物不存在");
        if (!pet.getOwnerId().equals(ownerId)) throw new ForbiddenException("只能修改自己的宠物状态");

        pet.setStatus(status);
        petMapper.updateById(pet);

        return PetVO.of(pet.getId(), pet.getName(), pet.getBreed(),
                pet.getGender(), pet.getAgeMonths(), pet.getWeightKg(),
                pet.getNeutered(), pet.getHealthStatus(),
                pet.getLocationProvince(), pet.getLocationCity(),
                pet.getDescription(), pet.getStatus(), pet.getViewCount(),
                null, pet.getCreatedAt());
    }

    // ────────────────── 我的宠物 ──────────────────

    @Override
    public PageResult<PetVO> getMyPets(Long ownerId, Integer status, int page, int size) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<Pet>()
                .eq(Pet::getOwnerId, ownerId);
        if (status != null) wrapper.eq(Pet::getStatus, status);
        wrapper.orderByDesc(Pet::getCreatedAt);

        Page<Pet> petPage = petMapper.selectPage(new Page<>(page, size), wrapper);
        List<Long> petIds = petPage.getRecords().stream().map(Pet::getId).toList();
        Map<Long, String> coverMap = buildCoverMap(petIds);

        List<PetVO> voList = petPage.getRecords().stream()
                .map(pet -> PetVO.of(
                        pet.getId(), pet.getName(), pet.getBreed(),
                        pet.getGender(), pet.getAgeMonths(), pet.getWeightKg(),
                        pet.getNeutered(), pet.getHealthStatus(),
                        pet.getLocationProvince(), pet.getLocationCity(),
                        pet.getDescription(), pet.getStatus(), pet.getViewCount(),
                        coverMap.get(pet.getId()), pet.getCreatedAt()))
                .toList();

        return PageResult.of(voList, petPage.getTotal(), (int) petPage.getCurrent(), (int) petPage.getSize());
    }

    // ────────────────── 热门宠物（Cache Aside + Redisson 防击穿） ──────────────────

    @Override
    public List<PetVO> getHotPets(int limit) {
        String key = RedisKeyConstant.HOT_PETS_KEY;
        // 1. Cache Aside：先读缓存
        String cached = cacheService.getString(key);
        if (cached != null) {
            return JSONUtil.toList(cached, PetVO.class);
        }
        // 2. 缓存未命中：尝试用 Redisson 分布式锁互斥回源（防缓存击穿）
        if (redissonClient != null) {
            RLock lock = redissonClient.getLock("lock:" + key);
            try {
                // 抢锁等待 3s；leaseTime=-1 启用看门狗：默认 30s 超时，每 10s 自动续期，业务未完锁不丢
                if (lock.tryLock(3, -1, TimeUnit.SECONDS)) {
                    try {
                        // 双重检查（DCL）：抢到锁后再看一眼缓存，避免并发重复回源
                        String recheck = cacheService.getString(key);
                        if (recheck != null) {
                            return JSONUtil.toList(recheck, PetVO.class);
                        }
                        List<PetVO> list = loadHotFromDb(limit);
                        cacheService.set(key, list, 1, TimeUnit.HOURS);
                        return list;
                    } finally {
                        if (lock.isHeldByCurrentThread()) {
                            lock.unlock();
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("获取热门宠物分布式锁被中断，降级为无锁直查 DB");
            } catch (Exception e) {
                log.warn("Redisson 锁回源异常，降级为无锁直查 DB: {}", e.getMessage());
            }
        }
        // 3. 降级：无 Redisson（本地未启动 Redis）或抢锁失败 → 直接查 DB 并写缓存
        List<PetVO> list = loadHotFromDb(limit);
        cacheService.set(key, list, 1, TimeUnit.HOURS);
        return list;
    }

    /** 从 DB 加载热门宠物并转 VO（缓存中不携带 createdAt，规避 LocalDateTime 序列化问题） */
    private List<PetVO> loadHotFromDb(int limit) {
        List<Pet> pets = petMapper.selectHotPets(limit);
        List<Long> petIds = pets.stream().map(Pet::getId).toList();
        Map<Long, String> coverMap = buildCoverMap(petIds);
        return pets.stream()
                .map(p -> PetVO.of(p.getId(), p.getName(), p.getBreed(),
                        p.getGender(), p.getAgeMonths(), p.getWeightKg(),
                        p.getNeutered(), p.getHealthStatus(),
                        p.getLocationProvince(), p.getLocationCity(),
                        p.getDescription(), p.getStatus(), p.getViewCount(),
                        coverMap.get(p.getId()), null))
                .toList();
    }

    // ────────────────── 图片上传 ──────────────────

    @Override
    @Transactional
    public List<PetImageVO> uploadPetImages(Long petId, MultipartFile[] files, Long userId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) throw new NotFoundException("宠物不存在");
        if (!pet.getOwnerId().equals(userId)) throw new ForbiddenException("只能上传自己宠物的图片");

        List<PetImageVO> result = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            if (file.isEmpty()) continue;

            String imageUrl = fileStorageStrategy.upload(file, "pets/" + petId);
            PetImage image = new PetImage();
            image.setPetId(petId);
            image.setImageUrl(imageUrl);
            image.setImageType(1); // 宠物照片
            image.setSortOrder(i);
            petImageMapper.insert(image);

            result.add(PetImageVO.of(image.getId(), image.getImageUrl(), 1, i));
        }
        return result;
    }

    // ────────────────── 图片删除 ──────────────────

    @Override
    public void deletePetImage(Long petId, Long imageId, Long userId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) throw new NotFoundException("宠物不存在");
        if (!pet.getOwnerId().equals(userId)) throw new ForbiddenException("只能删除自己宠物的图片");

        PetImage image = petImageMapper.selectById(imageId);
        if (image == null || !image.getPetId().equals(petId))
            throw new NotFoundException("图片不存在");

        fileStorageStrategy.delete(image.getImageUrl());
        petImageMapper.deleteById(imageId);
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

    /** 手机号脱敏 */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

}
