package com.shanhai.petplatform.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 领养申请实体 — t_adoption_application
 *
 * @author PetPlatform Team
 */
@Data
@TableName("t_adoption_application")
public class AdoptionApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 申请ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 宠物ID */
    private Long petId;

    /** 申请人ID */
    private Long applicantId;

    /** 住房类型: 1-自有房 2-租房 3-与家人同住 */
    private Integer housingType;

    /** 月收入 */
    private BigDecimal monthlyIncome;

    /** 养宠经验: 0-无 1-有过 2-正在养 */
    private Integer petExperience;

    /** 家人态度 */
    private String familyAttitude;

    /** 现有宠物情况 */
    private String currentPets;

    /** 申请理由 */
    private String reason;

    /** 状态: 0-待审核 1-审核中 2-已通过 3-已驳回 4-已取消 */
    private Integer status;

    /** 驳回原因 */
    private String rejectReason;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
