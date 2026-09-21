package com.shanhai.petplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanhai.petplatform.common.dto.request.FollowUpSubmitRequest;
import com.shanhai.petplatform.common.dto.response.FollowUpRecordVO;
import com.shanhai.petplatform.common.dto.response.FollowUpTaskVO;
import com.shanhai.petplatform.common.exception.BusinessException;
import com.shanhai.petplatform.common.exception.ForbiddenException;
import com.shanhai.petplatform.common.exception.NotFoundException;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.repository.entity.*;
import com.shanhai.petplatform.repository.mapper.*;
import com.shanhai.petplatform.service.FollowUpService;
import com.shanhai.petplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowUpServiceImpl implements FollowUpService {

    private final FollowUpTaskMapper taskMapper;
    private final FollowUpRecordMapper recordMapper;
    private final AdoptionRecordMapper adoptionRecordMapper;
    private final PetMapper petMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void generateFollowUpPlan(Long adoptionRecordId, int totalMonths) {
        AdoptionRecord record = adoptionRecordMapper.selectById(adoptionRecordId);
        if (record == null) return;
        for (int i = 1; i <= totalMonths; i++) {
            FollowUpTask task = new FollowUpTask();
            task.setAdoptionRecordId(adoptionRecordId);
            task.setPeriodNumber(i);
            task.setScheduledDate(record.getAdoptedAt().toLocalDate().plusMonths(i));
            task.setDueDate(task.getScheduledDate().plusDays(7));
            task.setStatus(0);
            taskMapper.insert(task);
        }
    }

    @Override
    public boolean hasFollowUpPlan(Long adoptionRecordId) {
        Long count = taskMapper.selectCount(
                new LambdaQueryWrapper<FollowUpTask>()
                        .eq(FollowUpTask::getAdoptionRecordId, adoptionRecordId));
        return count != null && count > 0;
    }

    @Override
    public PageResult<FollowUpTaskVO> getMyTasks(Long userId, Integer status, int page, int size) {
        // 先从领养记录中查该用户的记录ID
        List<Long> recordIds = adoptionRecordMapper.selectList(
                new LambdaQueryWrapper<AdoptionRecord>().eq(AdoptionRecord::getApplicantId, userId))
                .stream().map(AdoptionRecord::getId).toList();
        if (recordIds.isEmpty()) return PageResult.empty(page, size);

        LambdaQueryWrapper<FollowUpTask> w = new LambdaQueryWrapper<FollowUpTask>()
                .in(FollowUpTask::getAdoptionRecordId, recordIds);
        if (status != null) w.eq(FollowUpTask::getStatus, status);
        w.orderByAsc(FollowUpTask::getDueDate);

        Page<FollowUpTask> p = taskMapper.selectPage(new Page<>(page, size), w);
        List<FollowUpTaskVO> vos = p.getRecords().stream().map(t -> toTaskVO(t)).toList();
        return PageResult.of(vos, p.getTotal(), (int) p.getCurrent(), (int) p.getSize());
    }

    @Override
    public FollowUpTaskVO getTaskDetail(Long taskId, Long userId) {
        FollowUpTask t = taskMapper.selectById(taskId);
        if (t == null) throw new NotFoundException("任务不存在");
        return toTaskVO(t);
    }

    @Override
    @Transactional
    public FollowUpRecordVO submitFollowUp(Long taskId, FollowUpSubmitRequest req, Long userId) {
        FollowUpTask task = taskMapper.selectById(taskId);
        if (task == null) throw new NotFoundException("任务不存在");
        if (task.getStatus() != 0 && task.getStatus() != 1)
            throw new BusinessException("该任务已处理，不可重复提交");

        AdoptionRecord record = adoptionRecordMapper.selectById(task.getAdoptionRecordId());
        if (record == null || !record.getApplicantId().equals(userId))
            throw new ForbiddenException("只能提交自己的回访任务");

        // 创建回访记录
        FollowUpRecord fr = new FollowUpRecord();
        fr.setTaskId(taskId);
        fr.setUserId(userId);
        fr.setContent(req.getContent());
        fr.setImageUrl(req.getImageUrls() != null && !req.getImageUrls().isEmpty()
                ? String.join(",", req.getImageUrls()) : null);
        recordMapper.insert(fr);

        // 更新任务状态
        task.setStatus(2);
        taskMapper.updateById(task);

        // 通知送养人
        notificationService.notify(record.getAdopterId(), "回访已提交",
                "第" + task.getPeriodNumber() + "次回访已提交", 3, "follow_up", taskId);

        // 检查是否全部完成 → 宠物状态→COMPLETED
        Long count = taskMapper.selectCount(new LambdaQueryWrapper<FollowUpTask>()
                .eq(FollowUpTask::getAdoptionRecordId, task.getAdoptionRecordId())
                .ne(FollowUpTask::getStatus, 2));
        if (count == 0) {
            Pet pet = petMapper.selectById(record.getPetId());
            if (pet != null) {
                pet.setStatus(5);
                petMapper.updateById(pet);
            }
            record.setStatus(2);
            adoptionRecordMapper.updateById(record);
        }

        return FollowUpRecordVO.of(fr.getId(), fr.getTaskId(), fr.getContent(), fr.getImageUrl(), fr.getSubmittedAt());
    }

    @Override
    public PageResult<FollowUpRecordVO> getRecords(Long adoptionRecordId, int page, int size) {
        List<Long> taskIds = taskMapper.selectList(
                new LambdaQueryWrapper<FollowUpTask>()
                        .eq(FollowUpTask::getAdoptionRecordId, adoptionRecordId))
                .stream().map(FollowUpTask::getId).toList();

        if (taskIds.isEmpty()) return PageResult.empty(page, size);

        Page<FollowUpRecord> p = recordMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<FollowUpRecord>()
                        .in(FollowUpRecord::getTaskId, taskIds)
                        .orderByDesc(FollowUpRecord::getSubmittedAt));

        List<FollowUpRecordVO> vos = p.getRecords().stream()
                .map(r -> FollowUpRecordVO.of(r.getId(), r.getTaskId(), r.getContent(), r.getImageUrl(), r.getSubmittedAt()))
                .toList();
        return PageResult.of(vos, p.getTotal(), (int) p.getCurrent(), (int) p.getSize());
    }

    private FollowUpTaskVO toTaskVO(FollowUpTask t) {
        AdoptionRecord r = adoptionRecordMapper.selectById(t.getAdoptionRecordId());
        Pet pet = r != null ? petMapper.selectById(r.getPetId()) : null;
        return FollowUpTaskVO.of(t.getId(), t.getAdoptionRecordId(),
                pet != null ? pet.getName() : "未知",
                t.getPeriodNumber(), t.getScheduledDate(), t.getDueDate(),
                t.getStatus(), t.getNotifiedAt());
    }
}
