package com.shanhai.petplatform.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 领养记录实体 — t_adoption_record
 *
 * @author PetPlatform Team
 */
@Data
@TableName("t_adoption_record")
public class AdoptionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请ID */
    private Long applicationId;

    /** 宠物ID */
    private Long petId;

    /** 送养人ID */
    private Long adopterId;

    /** 领养人ID */
    private Long applicantId;

    /** 领养日期 */
    private LocalDateTime adoptedAt;

    /** 状态: 0-领养中 1-回访中 2-已完成 */
    private Integer status;

    /** 回访总月数 */
    private Integer followUpMonths;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}
