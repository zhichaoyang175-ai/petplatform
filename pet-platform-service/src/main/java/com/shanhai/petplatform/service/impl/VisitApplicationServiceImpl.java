package com.shanhai.petplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanhai.petplatform.common.dto.request.VisitBookRequest;
import com.shanhai.petplatform.common.dto.response.VisitVO;
import com.shanhai.petplatform.common.enums.NotificationTypeEnum;
import com.shanhai.petplatform.common.exception.BusinessException;
import com.shanhai.petplatform.common.exception.NotFoundException;
import com.shanhai.petplatform.repository.entity.Pet;
import com.shanhai.petplatform.repository.entity.User;
import com.shanhai.petplatform.repository.entity.VisitAppointment;
import com.shanhai.petplatform.repository.mapper.PetMapper;
import com.shanhai.petplatform.repository.mapper.UserMapper;
import com.shanhai.petplatform.repository.mapper.VisitAppointmentMapper;
import com.shanhai.petplatform.service.NotificationService;
import com.shanhai.petplatform.service.VisitApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 预约看宠服务实现
 *
 * @author PetPlatform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VisitApplicationServiceImpl implements VisitApplicationService {

    private static final String REF_TYPE = "visit";

    private final VisitAppointmentMapper visitMapper;
    private final PetMapper petMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    // ────────────────── 预约 ──────────────────

    @Override
    @Transactional
    public VisitVO book(VisitBookRequest request, Long applicantId) {
        Pet pet = petMapper.selectById(request.getPetId());
        if (pet == null) throw new NotFoundException("宠物不存在");

        VisitAppointment appointment = new VisitAppointment();
        appointment.setPetId(pet.getId());
        appointment.setApplicantId(applicantId);
        appointment.setOwnerId(pet.getOwnerId());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setNote(request.getNote());
        appointment.setStatus(0); // 待确认

        visitMapper.insert(appointment);
        log.info("发起看宠预约: visitId={}, petId={}, applicantId={}, ownerId={}",
                appointment.getId(), pet.getId(), applicantId, pet.getOwnerId());

        // 通知送养人
        notificationService.notify(pet.getOwnerId(), "新的看宠预约",
                "有人预约看您的宠物「" + pet.getName() + "」",
                NotificationTypeEnum.FOLLOW_UP_REMINDER.getCode(), REF_TYPE, appointment.getId());

        return toVO(appointment, pet);
    }

    // ────────────────── 列表 ──────────────────

    @Override
    public List<VisitVO> myVisits(Long applicantId) {
        List<VisitAppointment> list = visitMapper.selectList(
                new LambdaQueryWrapper<VisitAppointment>()
                        .eq(VisitAppointment::getApplicantId, applicantId)
                        .orderByDesc(VisitAppointment::getAppointmentTime));
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public List<VisitVO> receivedVisits(Long ownerId) {
        List<VisitAppointment> list = visitMapper.selectList(
                new LambdaQueryWrapper<VisitAppointment>()
                        .eq(VisitAppointment::getOwnerId, ownerId)
                        .orderByDesc(VisitAppointment::getAppointmentTime));
        return list.stream().map(this::toVO).toList();
    }

    // ────────────────── 状态变更 ──────────────────

    @Override
    @Transactional
    public VisitVO confirm(Long id, Long ownerId) {
        VisitAppointment appointment = requireVisit(id);
        if (!appointment.getOwnerId().equals(ownerId)) {
            throw new BusinessException(403, "只能确认自己宠物收到的预约");
        }
        if (appointment.getStatus() != 0) {
            throw new BusinessException("仅待确认的预约可以确认");
        }
        appointment.setStatus(1); // 已确认
        visitMapper.updateById(appointment);

        // 通知申请人
        Pet pet = petMapper.selectById(appointment.getPetId());
        notificationService.notify(appointment.getApplicantId(), "看宠预约已确认",
                "您的看宠预约（" + (pet != null ? pet.getName() : "宠物") + "）已被送养人确认",
                NotificationTypeEnum.FOLLOW_UP_REMINDER.getCode(), REF_TYPE, appointment.getId());

        return toVO(appointment, pet);
    }

    @Override
    @Transactional
    public VisitVO complete(Long id, Long userId) {
        VisitAppointment appointment = requireVisit(id);
        assertParticipant(appointment, userId);
        if (appointment.getStatus() != 1) {
            throw new BusinessException("仅已确认的预约可以完成");
        }
        appointment.setStatus(2); // 已完成
        visitMapper.updateById(appointment);

        notifyCounterparty(appointment, userId, "看宠预约已完成",
                "您参与的看宠预约已标记为完成");

        return toVO(appointment);
    }

    @Override
    @Transactional
    public VisitVO cancel(Long id, Long userId) {
        VisitAppointment appointment = requireVisit(id);
        assertParticipant(appointment, userId);
        if (appointment.getStatus() == 2) {
            throw new BusinessException("已完成的预约无法取消");
        }
        appointment.setStatus(3); // 已取消
        visitMapper.updateById(appointment);

        notifyCounterparty(appointment, userId, "看宠预约已取消",
                "您参与的看宠预约已被对方取消");

        return toVO(appointment);
    }

    // ────────────────── 工具 ──────────────────

    private VisitAppointment requireVisit(Long id) {
        VisitAppointment appointment = visitMapper.selectById(id);
        if (appointment == null) throw new NotFoundException("预约不存在");
        return appointment;
    }

    /** 校验操作人必须是申请人或送养人 */
    private void assertParticipant(VisitAppointment appointment, Long userId) {
        boolean isParticipant = userId.equals(appointment.getApplicantId())
                || userId.equals(appointment.getOwnerId());
        if (!isParticipant) {
            throw new BusinessException(403, "只能操作自己的预约");
        }
    }

    /** 通知预约的另一方 */
    private void notifyCounterparty(VisitAppointment appointment, Long actorId,
                                     String title, String content) {
        Long counterparty = actorId.equals(appointment.getApplicantId())
                ? appointment.getOwnerId()
                : appointment.getApplicantId();
        notificationService.notify(counterparty, title, content,
                NotificationTypeEnum.SYSTEM.getCode(), REF_TYPE, appointment.getId());
    }

    /** 通过预约 + 宠物构建 VO */
    private VisitVO toVO(VisitAppointment appointment, Pet pet) {
        String petName = pet != null ? pet.getName() : "未知宠物";
        String applicantName = userName(appointment.getApplicantId());
        String ownerName = userName(appointment.getOwnerId());
        return VisitVO.of(appointment.getId(), appointment.getPetId(), appointment.getApplicantId(),
                appointment.getOwnerId(), petName, applicantName, ownerName,
                appointment.getAppointmentTime(), appointment.getNote(), appointment.getStatus(),
                appointment.getCreatedAt(), appointment.getUpdatedAt());
    }

    /** 不额外查宠物时的 VO 构建（状态变更后） */
    private VisitVO toVO(VisitAppointment appointment) {
        return toVO(appointment, petMapper.selectById(appointment.getPetId()));
    }

    private String userName(Long userId) {
        if (userId == null) return "未知用户";
        User user = userMapper.selectById(userId);
        return user != null ? (user.getNickname() != null ? user.getNickname() : "用户") : "未知用户";
    }

}
