package com.shanhai.petplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanhai.petplatform.common.dto.response.PetImageVO;
import com.shanhai.petplatform.common.dto.response.PetVO;
import com.shanhai.petplatform.common.exception.BusinessException;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.repository.entity.Favorite;
import com.shanhai.petplatform.repository.entity.Pet;
import com.shanhai.petplatform.repository.entity.PetImage;
import com.shanhai.petplatform.repository.mapper.FavoriteMapper;
import com.shanhai.petplatform.repository.mapper.PetImageMapper;
import com.shanhai.petplatform.repository.mapper.PetMapper;
import com.shanhai.petplatform.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final PetMapper petMapper;
    private final PetImageMapper petImageMapper;

    @Override
    public void addFavorite(Long petId, Long userId) {
        if (petMapper.selectById(petId) == null) throw new BusinessException("宠物不存在");
        if (favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId).eq(Favorite::getPetId, petId)) > 0) return;
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setPetId(petId);
        favoriteMapper.insert(f);
    }

    @Override
    public void removeFavorite(Long petId, Long userId) {
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId).eq(Favorite::getPetId, petId));
    }

    @Override
    public PageResult<PetVO> getFavorites(Long userId, int page, int size) {
        Page<Favorite> fPage = favoriteMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId).orderByDesc(Favorite::getCreatedAt));

        Set<Long> petIds = fPage.getRecords().stream().map(Favorite::getPetId).collect(Collectors.toSet());
        if (petIds.isEmpty()) return PageResult.empty(page, size);

        List<Pet> pets = petMapper.selectBatchIds(petIds);
        Map<Long, Pet> petMap = pets.stream().collect(Collectors.toMap(Pet::getId, p -> p));

        // 封面图
        List<PetImage> covers = petImageMapper.selectList(new LambdaQueryWrapper<PetImage>()
                .in(PetImage::getPetId, petIds).eq(PetImage::getImageType, 1).eq(PetImage::getSortOrder, 0));
        Map<Long, String> coverMap = covers.stream().collect(Collectors.toMap(PetImage::getPetId, PetImage::getImageUrl, (a, b) -> a));

        List<PetVO> vos = fPage.getRecords().stream().map(f -> {
            Pet p = petMap.get(f.getPetId());
            if (p == null) return null;
            return PetVO.of(p.getId(), p.getName(), p.getBreed(), p.getGender(), p.getAgeMonths(), p.getWeightKg(),
                    p.getNeutered(), p.getHealthStatus(), p.getLocationProvince(), p.getLocationCity(),
                    p.getDescription(), p.getStatus(), p.getViewCount(), coverMap.get(p.getId()), p.getCreatedAt());
        }).filter(v -> v != null).toList();

        return PageResult.of(vos, fPage.getTotal(), (int) fPage.getCurrent(), (int) fPage.getSize());
    }

    @Override
    public boolean isFavorited(Long petId, Long userId) {
        return favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId).eq(Favorite::getPetId, petId)) > 0;
    }
}
