package com.shanhai.petplatform.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知实体 — t_notification
 *
 * @author PetPlatform Team
 */
@Data
@TableName("t_notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 通知ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户ID */
    private Long userId;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 类型: 1-申请状态 2-审核结果 3-回访提醒 4-系统通知 */
    private Integer type;

    /** 阅读状态: 0-未读 1-已读 */
    private Integer readStatus;

    /** 关联类型: application/pet/follow_up */
    private String refType;

    /** 关联ID */
    private Long refId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}
