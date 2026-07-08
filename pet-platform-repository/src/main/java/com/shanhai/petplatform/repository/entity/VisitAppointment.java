package com.shanhai.petplatform.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预约看宠（线下探视）实体 — t_visit_appointment
 *
 * @author PetPlatform Team
 */
@Data
@TableName("t_visit_appointment")
public class VisitAppointment implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 预约ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 宠物ID */
    private Long petId;

    /** 申请人ID（领养人） */
    private Long applicantId;

    /** 宠物主人/送养人ID */
    private Long ownerId;

    /** 预约探视时间 */
    private LocalDateTime appointmentTime;

    /** 状态: 0-待确认 1-已确认 2-已完成 3-已取消 */
    private Integer status;

    /** 备注/留言 */
    private String note;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
