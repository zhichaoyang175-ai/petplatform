package com.shanhai.petplatform.repository.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通用审核任务表
 *
 * @author PetPlatform Team
 */
@Data
@TableName("t_review_task")
public class ReviewTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请人ID */
    private Long applicantId;

    /** 审核类型：ReviewTypeEnum */
    private Integer type;

    /** 审核状态：ReviewStatusEnum */
    private Integer status;

    /** 审核标题 */
    private String title;

    /** 提交资料（JSON） */
    private String submitData;

    /** 审核人ID */
    private Long reviewerId;

    /** 审核意见 */
    private String reviewComment;

    /** 审核时间 */
    private LocalDateTime reviewedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
