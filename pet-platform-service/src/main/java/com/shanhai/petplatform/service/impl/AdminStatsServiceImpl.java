package com.shanhai.petplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanhai.petplatform.common.dto.response.AdminStatsVO;
import com.shanhai.petplatform.common.exception.BusinessException;
import com.shanhai.petplatform.repository.entity.AdoptionApplication;
import com.shanhai.petplatform.repository.entity.AdoptionRecord;
import com.shanhai.petplatform.repository.entity.Pet;
import com.shanhai.petplatform.repository.entity.User;
import com.shanhai.petplatform.repository.mapper.AdoptionApplicationMapper;
import com.shanhai.petplatform.repository.mapper.AdoptionRecordMapper;
import com.shanhai.petplatform.repository.mapper.PetMapper;
import com.shanhai.petplatform.repository.mapper.UserMapper;
import com.shanhai.petplatform.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理后台 KPI 统计服务实现
 *
 * @author PetPlatform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStatsServiceImpl implements AdminStatsService {

    /** 送养人角色 */
    private static final int ROLE_ADOPTER = 2;

    /** 宠物待领养状态 */
    private static final int PET_PENDING = 0;

    /** 领养申请待审核状态 */
    private static final int APPLICATION_PENDING = 0;

    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final AdoptionApplicationMapper applicationMapper;
    private final AdoptionRecordMapper recordMapper;

    @Override
    public AdminStatsVO getDashboardStats() {
        try {
            AdminStatsVO vo = new AdminStatsVO();
            vo.setUserTotal(userMapper.selectCount(null));
            vo.setAdopterCount(userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getRole, ROLE_ADOPTER)));
            vo.setPetTotal(petMapper.selectCount(null));
            vo.setPendingPetCount(petMapper.selectCount(
                    new LambdaQueryWrapper<Pet>().eq(Pet::getStatus, PET_PENDING)));
            vo.setApplicationTotal(applicationMapper.selectCount(null));
            vo.setPendingApplicationCount(applicationMapper.selectCount(
                    new LambdaQueryWrapper<AdoptionApplication>().eq(AdoptionApplication::getStatus, APPLICATION_PENDING)));
            vo.setAdoptedCount(recordMapper.selectCount(null));
            // 当前数据模型未包含 t_shelter 表，合作救助站数返回 0
            vo.setShelterCount(0L);

            // 最近审核申请（仪表盘「申请审核」列表，点击进入 /admin/review/:id）
            List<AdminStatsVO.RecentReview> recent = applicationMapper.selectPage(
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10),
                    new LambdaQueryWrapper<AdoptionApplication>().orderByDesc(AdoptionApplication::getCreatedAt))
                    .getRecords().stream().map(a -> {
                        Pet p = petMapper.selectById(a.getPetId());
                        User u = userMapper.selectById(a.getApplicantId());
                        AdminStatsVO.RecentReview r = new AdminStatsVO.RecentReview();
                        r.setId(a.getId());
                        r.setApplicantName(u != null ? u.getNickname() : "未知");
                        r.setPetName(p != null ? p.getName() : "未知");
                        r.setStatusName(applicationStatusName(a.getStatus()));
                        r.setStatusType(applicationStatusType(a.getStatus()));
                        return r;
                    }).toList();
            vo.setRecentReviews(recent);

            return vo;
        } catch (Exception e) {
            log.error("管理后台 KPI 统计失败", e);
            throw new BusinessException("统计数据获取失败");
        }
    }

    private String applicationStatusName(Integer status) {
        return switch (status == null ? -1 : status) {
            case 0 -> "待审核";
            case 1 -> "审核中";
            case 2 -> "已通过";
            case 3 -> "已驳回";
            case 4 -> "已取消";
            default -> "未知";
        };
    }

    private String applicationStatusType(Integer status) {
        return switch (status == null ? -1 : status) {
            case 0 -> "warning";
            case 1 -> "warning";
            case 2 -> "success";
            case 3 -> "error";
            case 4 -> "muted";
            default -> "info";
        };
    }
}
