package com.shanhai.petplatform.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价/信用实体 — t_evaluation
 *
 * @author PetPlatform Team
 */
@Data
@TableName("t_evaluation")
public class Evaluation implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评价ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 评价人ID */
    private Long reviewerId;

    /** 评价目标类型: 1-用户 2-救助站 */
    private Integer targetType;

    /** 评价目标ID */
    private Long targetId;

    /** 评分: 1-5 */
    private Integer score;

    /** 评价内容 */
    private String comment;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
