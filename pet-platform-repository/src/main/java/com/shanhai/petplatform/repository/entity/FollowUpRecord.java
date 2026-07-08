package com.shanhai.petplatform.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 回访记录实体 — t_follow_up_record
 *
 * @author PetPlatform Team
 */
@Data
@TableName("t_follow_up_record")
public class FollowUpRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务ID(一对一) */
    private Long taskId;

    /** 上传用户ID */
    private Long userId;

    /** 文字描述 */
    private String content;

    /** 回访照片URL */
    private String imageUrl;

    /** 提交时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime submittedAt;

}
