package com.shanhai.petplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanhai.petplatform.common.dto.response.AdoptionRecordVO;
import com.shanhai.petplatform.common.exception.NotFoundException;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.repository.entity.AdoptionRecord;
import com.shanhai.petplatform.repository.entity.Pet;
import com.shanhai.petplatform.repository.entity.User;
import com.shanhai.petplatform.repository.mapper.AdoptionRecordMapper;
import com.shanhai.petplatform.repository.mapper.PetMapper;
import com.shanhai.petplatform.repository.mapper.UserMapper;
import com.shanhai.petplatform.service.AdoptionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdoptionRecordServiceImpl implements AdoptionRecordService {

    private final AdoptionRecordMapper recordMapper;
    private final PetMapper petMapper;
    private final UserMapper userMapper;

    @Override
    public AdoptionRecordVO getAdoptionRecord(Long id) {
        AdoptionRecord r = recordMapper.selectById(id);
        if (r == null) throw new NotFoundException("领养记录不存在");
        return toVO(r);
    }

    @Override
    public PageResult<AdoptionRecordVO> getMyAdoptions(Long userId, Integer status, int page, int size) {
        LambdaQueryWrapper<AdoptionRecord> w = new LambdaQueryWrapper<AdoptionRecord>()
                .eq(AdoptionRecord::getApplicantId, userId);
        if (status != null) w.eq(AdoptionRecord::getStatus, status);
        w.orderByDesc(AdoptionRecord::getCreatedAt);

        Page<AdoptionRecord> p = recordMapper.selectPage(new Page<>(page, size), w);
        List<AdoptionRecordVO> vos = p.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(vos, p.getTotal(), (int) p.getCurrent(), (int) p.getSize());
    }

    @Override
    public PageResult<AdoptionRecordVO> getMySentPets(Long userId, Integer status, int page, int size) {
        LambdaQueryWrapper<AdoptionRecord> w = new LambdaQueryWrapper<AdoptionRecord>()
                .eq(AdoptionRecord::getAdopterId, userId);
        if (status != null) w.eq(AdoptionRecord::getStatus, status);
        w.orderByDesc(AdoptionRecord::getCreatedAt);

        Page<AdoptionRecord> p = recordMapper.selectPage(new Page<>(page, size), w);
        List<AdoptionRecordVO> vos = p.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(vos, p.getTotal(), (int) p.getCurrent(), (int) p.getSize());
    }

    private AdoptionRecordVO toVO(AdoptionRecord r) {
        Pet pet = petMapper.selectById(r.getPetId());
        User adopter = userMapper.selectById(r.getAdopterId());
        User applicant = userMapper.selectById(r.getApplicantId());
        return AdoptionRecordVO.of(r.getId(), r.getApplicationId(),
                pet != null ? pet.getName() : "未知",
                pet != null ? pet.getBreed() : "未知",
                adopter != null ? adopter.getNickname() : "未知",
                applicant != null ? applicant.getNickname() : "未知",
                r.getAdoptedAt(), r.getStatus(), r.getFollowUpMonths());
    }
}
