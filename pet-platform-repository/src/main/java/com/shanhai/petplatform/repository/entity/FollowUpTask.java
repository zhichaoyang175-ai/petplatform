package com.shanhai.petplatform.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 回访任务实体 — t_follow_up_task
 *
 * @author PetPlatform Team
 */
@Data
@TableName("t_follow_up_task")
public class FollowUpTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 任务ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 领养记录ID */
    private Long adoptionRecordId;

    /** 第几次回访(1/2/3...) */
    private Integer periodNumber;

    /** 计划回访日期 */
    private LocalDate scheduledDate;

    /** 截止日期 */
    private LocalDate dueDate;

    /** 状态: 0-待执行 1-已提醒 2-已完成 3-已逾期 */
    private Integer status;

    /** 提醒通知时间 */
    private LocalDateTime notifiedAt;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}
