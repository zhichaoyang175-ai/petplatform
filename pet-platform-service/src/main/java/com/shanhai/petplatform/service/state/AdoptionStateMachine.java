package com.shanhai.petplatform.service.state;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shanhai.petplatform.common.dto.request.ApplicationReviewRequest;
import com.shanhai.petplatform.common.dto.request.ApplicationSubmitRequest;
import com.shanhai.petplatform.common.dto.response.ApplicationVO;
import com.shanhai.petplatform.common.exception.BusinessException;
import com.shanhai.petplatform.common.exception.ForbiddenException;
import com.shanhai.petplatform.common.exception.NotFoundException;
import com.shanhai.petplatform.repository.entity.*;
import com.shanhai.petplatform.repository.mapper.*;
import com.shanhai.petplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 领养状态机 — 枚举 + 事件驱动模式，统一管理所有状态转换
 *
 * @author PetPlatform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdoptionStateMachine {

    private final PetMapper petMapper;
    private final AdoptionApplicationMapper applicationMapper;
    private final AdoptionRecordMapper recordMapper;
    private final FollowUpTaskMapper followUpTaskMapper;
    private final NotificationService notificationService;

    private static final String EMOJI_DOG = "🐕";
    private static final String EMOJI_CAT = "🐱";

    // ──────────────── 提交申请 ────────────────

    @Transactional
    public ApplicationVO submitApplication(ApplicationSubmitRequest req, Long applicantId) {
        Pet pet = petMapper.selectById(req.getPetId());
        if (pet == null) throw new NotFoundException("宠物不存在");
        if (pet.getStatus() != 0) throw new BusinessException("该宠物已不可申请");
        if (pet.getOwnerId().equals(applicantId)) throw new BusinessException("不能申请自己发布的宠物");

        // 唯一索引防重
        if (applicationMapper.selectCount(new LambdaQueryWrapper<AdoptionApplication>()
                .eq(AdoptionApplication::getPetId, req.getPetId())
                .eq(AdoptionApplication::getApplicantId, applicantId)) > 0) {
            throw new BusinessException("您已申请过该宠物");
        }

        // 创建申请
        AdoptionApplication app = new AdoptionApplication();
        app.setPetId(req.getPetId());
        app.setApplicantId(applicantId);
        app.setHousingType(req.getHousingType());
        app.setMonthlyIncome(req.getMonthlyIncome());
        app.setPetExperience(req.getPetExperience());
        app.setFamilyAttitude(req.getFamilyAttitude());
        app.setCurrentPets(req.getCurrentPets());
        app.setReason(req.getReason());
        app.setStatus(0); // PENDING
        applicationMapper.insert(app);

        // 宠物 → 申请中
        pet.setStatus(1);
        petMapper.updateById(pet);

        // 通知送养人
        notificationService.notify(pet.getOwnerId(), "新的领养申请",
                "有人申请领养您的宠物「" + pet.getName() + "」", 1, "application", app.getId());

        log.info("领养申请提交: appId={}, petId={}, applicantId={}", app.getId(), req.getPetId(), applicantId);
        return buildApplicationVO(app, pet);
    }

    // ──────────────── 审核申请 ────────────────

    @Transactional
    public ApplicationVO reviewApplication(Long applicationId, ApplicationReviewRequest req, Long reviewerId) {
        AdoptionApplication app = applicationMapper.selectById(applicationId);
        if (app == null) throw new NotFoundException("申请不存在");

        Pet pet = petMapper.selectById(app.getPetId());
        if (pet == null) throw new NotFoundException("宠物不存在");

        // 只有送养人可以审核
        if (!pet.getOwnerId().equals(reviewerId)) throw new ForbiddenException("只能审核自己宠物的申请");

        if ("approve".equals(req.getAction())) {
            return approveApplication(app, pet);
        } else if ("reject".equals(req.getAction())) {
            return rejectApplication(app, pet, req.getRejectReason());
        }
        throw new BusinessException("未知的审核操作: " + req.getAction());
    }

    // ──────────────── 管理员审核（跳过送养人归属校验） ────────────────

    @Transactional
    public ApplicationVO adminReview(Long applicationId, String action, String rejectReason) {
        AdoptionApplication app = applicationMapper.selectById(applicationId);
        if (app == null) throw new NotFoundException("申请不存在");
        Pet pet = petMapper.selectById(app.getPetId());
        if (pet == null) throw new NotFoundException("宠物不存在");
        if ("approve".equals(action)) return approveApplication(app, pet);
        if ("reject".equals(action)) return rejectApplication(app, pet, rejectReason);
        throw new BusinessException("未知的审核操作: " + action);
    }

    private ApplicationVO approveApplication(AdoptionApplication app, Pet pet) {
        app.setStatus(2); // APPROVED
        applicationMapper.updateById(app);

        // 创建领养记录
        AdoptionRecord record = new AdoptionRecord();
        record.setApplicationId(app.getId());
        record.setPetId(pet.getId());
        record.setAdopterId(pet.getOwnerId());
        record.setApplicantId(app.getApplicantId());
        record.setAdoptedAt(LocalDateTime.now());
        record.setStatus(0); // ADOPTING
        record.setFollowUpMonths(12);
        recordMapper.insert(record);

        // 生成回访计划
        generateFollowUpPlan(record);

        // 宠物 → 已领养
        pet.setStatus(3);
        petMapper.updateById(pet);

        // 拒绝同宠物其他申请
        applicationMapper.update(null, new LambdaUpdateWrapper<AdoptionApplication>()
                .eq(AdoptionApplication::getPetId, pet.getId())
                .ne(AdoptionApplication::getId, app.getId())
                .in(AdoptionApplication::getStatus, 0, 1)
                .set(AdoptionApplication::getStatus, 3)
                .set(AdoptionApplication::getRejectReason, "已被其他人领养"));

        // 通知领养人
        notificationService.notify(app.getApplicantId(), "申请已通过",
                "恭喜！您的领养申请（" + pet.getName() + "）已通过审核", 2, "application", app.getId());

        log.info("审核通过: appId={}, petId={}", app.getId(), pet.getId());
        return buildApplicationVO(app, pet);
    }

    private ApplicationVO rejectApplication(AdoptionApplication app, Pet pet, String reason) {
        app.setStatus(3); // REJECTED
        app.setRejectReason(reason);
        applicationMapper.updateById(app);

        // 宠物 → 待领养
        pet.setStatus(0);
        petMapper.updateById(pet);

        notificationService.notify(app.getApplicantId(), "申请被驳回",
                "您的领养申请（" + pet.getName() + "）已被驳回" +
                        (reason != null ? "，原因：" + reason : ""), 2, "application", app.getId());

        log.info("审核驳回: appId={}", app.getId());
        return buildApplicationVO(app, pet);
    }

    // ──────────────── 取消申请 ────────────────

    @Transactional
    public ApplicationVO cancelApplication(Long applicationId, Long applicantId) {
        AdoptionApplication app = applicationMapper.selectById(applicationId);
        if (app == null) throw new NotFoundException("申请不存在");
        if (!app.getApplicantId().equals(applicantId)) throw new ForbiddenException("只能取消自己的申请");

        // 只有 PENDING 或 REVIEWING 状态可取消
        if (app.getStatus() != 0 && app.getStatus() != 1)
            throw new BusinessException("当前申请状态不可取消");

        app.setStatus(4); // CANCELLED
        applicationMapper.updateById(app);

        Pet pet = petMapper.selectById(app.getPetId());
        pet.setStatus(0); // AVAILABLE
        petMapper.updateById(pet);

        return buildApplicationVO(app, pet);
    }

    // ──────────────── 回访计划生成 ────────────────

    private void generateFollowUpPlan(AdoptionRecord record) {
        for (int i = 1; i <= record.getFollowUpMonths(); i++) {
            FollowUpTask task = new FollowUpTask();
            task.setAdoptionRecordId(record.getId());
            task.setPeriodNumber(i);
            task.setScheduledDate(record.getAdoptedAt().toLocalDate().plusMonths(i));
            task.setDueDate(task.getScheduledDate().plusDays(7));
            task.setStatus(0); // PENDING
            followUpTaskMapper.insert(task);
        }
    }

    // ──────────────── 构建 VO ────────────────

    private ApplicationVO buildApplicationVO(AdoptionApplication app, Pet pet) {
        return ApplicationVO.of(
                app.getId(), app.getPetId(), pet.getName(), pet.getBreed(),
                petEmoji(pet.getBreed()), app.getApplicantId(), null,
                app.getHousingType(), app.getPetExperience(),
                app.getMonthlyIncome(), app.getFamilyAttitude(),
                app.getCurrentPets(), app.getReason(),
                app.getStatus(), app.getRejectReason(), app.getCreatedAt());
    }

    private String petEmoji(String breed) {
        if (breed == null) return EMOJI_DOG;
        return breed.contains("猫") ? EMOJI_CAT : EMOJI_DOG;
    }

}
