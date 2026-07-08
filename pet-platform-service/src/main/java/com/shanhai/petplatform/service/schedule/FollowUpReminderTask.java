package com.shanhai.petplatform.service.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanhai.petplatform.repository.entity.AdoptionRecord;
import com.shanhai.petplatform.repository.entity.FollowUpTask;
import com.shanhai.petplatform.repository.mapper.AdoptionRecordMapper;
import com.shanhai.petplatform.repository.mapper.FollowUpTaskMapper;
import com.shanhai.petplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowUpReminderTask {

    private final FollowUpTaskMapper taskMapper;
    private final AdoptionRecordMapper recordMapper;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * ?")
    @SchedulerLock(name = "followUpReminder", lockAtLeastFor = "PT30S", lockAtMostFor = "PT5M")
    public void remindFollowUp() {
        log.info("回访提醒定时任务开始");

        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);

        // a. 查询未来3天内到期的待执行任务
        List<FollowUpTask> upcomingTasks = taskMapper.selectList(
                new LambdaQueryWrapper<FollowUpTask>()
                        .eq(FollowUpTask::getStatus, 0)
                        .between(FollowUpTask::getDueDate, today, threeDaysLater));

        for (FollowUpTask task : upcomingTasks) {
            task.setStatus(1); // REMINDED
            task.setNotifiedAt(LocalDateTime.now());
            taskMapper.updateById(task);

            AdoptionRecord record = recordMapper.selectById(task.getAdoptionRecordId());
            if (record != null) {
                notificationService.notify(record.getApplicantId(), "回访提醒",
                        "您的第" + task.getPeriodNumber() + "次回访任务即将到期，请尽快上传近况照片",
                        3, "follow_up", task.getId());
            }
        }

        // d. 过期处理
        List<FollowUpTask> overdueTasks = taskMapper.selectList(
                new LambdaQueryWrapper<FollowUpTask>()
                        .lt(FollowUpTask::getDueDate, today)
                        .ne(FollowUpTask::getStatus, 2));
        for (FollowUpTask task : overdueTasks) {
            task.setStatus(3); // OVERDUE
            taskMapper.updateById(task);
        }

        log.info("回访提醒完成: 提醒{}条, 逾期{}条", upcomingTasks.size(), overdueTasks.size());
    }
}
