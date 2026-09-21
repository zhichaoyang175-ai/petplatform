package com.shanhai.petplatform.service.state;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shanhai.petplatform.common.dto.request.ApplicationReviewRequest;
import com.shanhai.petplatform.common.dto.request.ApplicationSubmitRequest;
import com.shanhai.petplatform.common.dto.response.ApplicationVO;
import com.shanhai.petplatform.common.enums.ApplicationStatusEnum;
import com.shanhai.petplatform.common.enums.PetStatusEnum;
import com.shanhai.petplatform.common.exception.BusinessException;
import com.shanhai.petplatform.common.exception.ForbiddenException;
import com.shanhai.petplatform.common.exception.NotFoundException;
import com.shanhai.petplatform.repository.entity.*;
import com.shanhai.petplatform.repository.mapper.*;
import com.shanhai.petplatform.service.state.event.ApplicationApprovedEvent;
import com.shanhai.petplatform.service.state.event.ApplicationRejectedEvent;
import com.shanhai.petplatform.service.state.event.ApplicationSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 领养状态机 — 统一管理所有状态转换。
 *
 * <p>采用 Spring 领域事件实现真正的事件驱动：状态机只负责状态流转与业务一致性，
 * 通过 {@code ApplicationEventPublisher} 发布「申请提交 / 通过 / 驳回」等状态事件；
 * 通知等副作用由独立监听器在事务提交后处理，状态机与通知子系统彻底解耦。
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
    private final ApplicationEventPublisher eventPublisher;

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
        app.setStatus(ApplicationStatusEnum.PENDING.getCode());
        applicationMapper.insert(app);

        // 宠物 → 申请中（条件更新 CAS：仅"待领养"可被占用，
        // 避免与并发审核/领养交叉，导致已领养宠物被重新置为"申请中"）
        int locked = petMapper.update(null, new LambdaUpdateWrapper<Pet>()
                .eq(Pet::getId, pet.getId())
                .eq(Pet::getStatus, PetStatusEnum.AVAILABLE.getCode())
                .set(Pet::getStatus, PetStatusEnum.APPLIED.getCode()));
        if (locked == 0) {
            throw new BusinessException("该宠物已不可申请");
        }
        pet.setStatus(PetStatusEnum.APPLIED.getCode());

        // 发布领域事件：通知送养人（由监听器在事务提交后处理）
        eventPublisher.publishEvent(new ApplicationSubmittedEvent(
                app.getId(), pet.getId(), pet.getOwnerId(), pet.getName()));

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
        // ① 条件更新（CAS）：仅"待审核"可流转为"已通过"，影响行数为 0 说明已被并发处理
        int approvedRows = applicationMapper.update(null, new LambdaUpdateWrapper<AdoptionApplication>()
                .eq(AdoptionApplication::getId, app.getId())
                .eq(AdoptionApplication::getStatus, ApplicationStatusEnum.PENDING.getCode())
                .set(AdoptionApplication::getStatus, ApplicationStatusEnum.APPROVED.getCode()));
        if (approvedRows == 0) {
            throw new BusinessException("该申请已处理，无法重复审核");
        }
        app.setStatus(ApplicationStatusEnum.APPROVED.getCode());

        // ② 条件更新（CAS）：仅"尚未领养"的宠物可流转为"已领养"。
        // 关键：防止同一只宠物的两条不同申请被并发批准，产生两条互相冲突的领养记录
        int adoptedRows = petMapper.update(null, new LambdaUpdateWrapper<Pet>()
                .eq(Pet::getId, pet.getId())
                .in(Pet::getStatus,
                        PetStatusEnum.AVAILABLE.getCode(),
                        PetStatusEnum.APPLIED.getCode(),
                        PetStatusEnum.REVIEWING.getCode())
                .set(Pet::getStatus, PetStatusEnum.ADOPTED.getCode()));
        if (adoptedRows == 0) {
            throw new BusinessException("该宠物已被领养，无法重复操作");
        }
        pet.setStatus(PetStatusEnum.ADOPTED.getCode());

        // 创建领养记录（application_id 唯一约束 + 上面的宠物 CAS 双重保证不会重复领养）
        AdoptionRecord record = new AdoptionRecord();
        record.setApplicationId(app.getId());
        record.setPetId(pet.getId());
        record.setAdopterId(pet.getOwnerId());
        record.setApplicantId(app.getApplicantId());
        record.setAdoptedAt(LocalDateTime.now());
        record.setStatus(0); // ADOPTING
        record.setFollowUpMonths(12);
        recordMapper.insert(record);

        // 拒绝同宠物其他申请（条件更新：仅"待审核 / 审核中"的其余申请会被置为已驳回）
        applicationMapper.update(null, new LambdaUpdateWrapper<AdoptionApplication>()
                .eq(AdoptionApplication::getPetId, pet.getId())
                .ne(AdoptionApplication::getId, app.getId())
                .in(AdoptionApplication::getStatus,
                        ApplicationStatusEnum.PENDING.getCode(),
                        ApplicationStatusEnum.REVIEWING.getCode())
                .set(AdoptionApplication::getStatus, ApplicationStatusEnum.REJECTED.getCode())
                .set(AdoptionApplication::getRejectReason, "已被其他人领养"));

        // 发布领域事件：通知领养人 + 异步生成回访计划（由监听器在事务提交后处理）
        eventPublisher.publishEvent(new ApplicationApprovedEvent(
                app.getId(), pet.getId(), app.getApplicantId(), pet.getName(),
                record.getId(), record.getFollowUpMonths(), record.getAdoptedAt().toLocalDate()));

        log.info("审核通过: appId={}, petId={}", app.getId(), pet.getId());
        return buildApplicationVO(app, pet);
    }

    private ApplicationVO rejectApplication(AdoptionApplication app, Pet pet, String reason) {
        // 条件更新（CAS）：仅"待审核"可流转为"已驳回"，防止并发下重复审核
        int rejectedRows = applicationMapper.update(null, new LambdaUpdateWrapper<AdoptionApplication>()
                .eq(AdoptionApplication::getId, app.getId())
                .eq(AdoptionApplication::getStatus, ApplicationStatusEnum.PENDING.getCode())
                .set(AdoptionApplication::getStatus, ApplicationStatusEnum.REJECTED.getCode())
                .set(AdoptionApplication::getRejectReason, reason));
        if (rejectedRows == 0) {
            throw new BusinessException("该申请已处理，无法重复审核");
        }
        app.setStatus(ApplicationStatusEnum.REJECTED.getCode());
        app.setRejectReason(reason);

        // 宠物 → 待领养（条件更新：若期间已被他人领养则保持原状，不回退）
        petMapper.update(null, new LambdaUpdateWrapper<Pet>()
                .eq(Pet::getId, pet.getId())
                .in(Pet::getStatus,
                        PetStatusEnum.AVAILABLE.getCode(),
                        PetStatusEnum.APPLIED.getCode(),
                        PetStatusEnum.REVIEWING.getCode())
                .set(Pet::getStatus, PetStatusEnum.AVAILABLE.getCode()));
        pet.setStatus(PetStatusEnum.AVAILABLE.getCode());

        // 发布领域事件：通知领养人申请被驳回（由监听器在事务提交后处理）
        eventPublisher.publishEvent(new ApplicationRejectedEvent(
                app.getId(), pet.getId(), app.getApplicantId(), pet.getName(), reason));

        log.info("审核驳回: appId={}", app.getId());
        return buildApplicationVO(app, pet);
    }

    // ──────────────── 取消申请 ────────────────

    @Transactional
    public ApplicationVO cancelApplication(Long applicationId, Long applicantId) {
        AdoptionApplication app = applicationMapper.selectById(applicationId);
        if (app == null) throw new NotFoundException("申请不存在");
        if (!app.getApplicantId().equals(applicantId)) throw new ForbiddenException("只能取消自己的申请");

        // 条件更新（CAS）：仅"待审核 / 审核中"可流转为"已取消"，影响行数为 0 即状态已被并发改变
        int cancelledRows = applicationMapper.update(null, new LambdaUpdateWrapper<AdoptionApplication>()
                .eq(AdoptionApplication::getId, app.getId())
                .in(AdoptionApplication::getStatus,
                        ApplicationStatusEnum.PENDING.getCode(),
                        ApplicationStatusEnum.REVIEWING.getCode())
                .set(AdoptionApplication::getStatus, ApplicationStatusEnum.CANCELLED.getCode()));
        if (cancelledRows == 0) {
            throw new BusinessException("当前申请状态不可取消");
        }
        app.setStatus(ApplicationStatusEnum.CANCELLED.getCode());

        Pet pet = petMapper.selectById(app.getPetId());
        // 宠物 → 待领养（条件更新：若期间已被他人领养则保持原状，不回退）
        petMapper.update(null, new LambdaUpdateWrapper<Pet>()
                .eq(Pet::getId, pet.getId())
                .in(Pet::getStatus,
                        PetStatusEnum.AVAILABLE.getCode(),
                        PetStatusEnum.APPLIED.getCode(),
                        PetStatusEnum.REVIEWING.getCode())
                .set(Pet::getStatus, PetStatusEnum.AVAILABLE.getCode()));
        pet.setStatus(PetStatusEnum.AVAILABLE.getCode());

        return buildApplicationVO(app, pet);
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
