package com.shanhai.petplatform.common.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 站内通知消息 — 用于通知的可靠投递（落点 B）。
 *
 * @author PetPlatform Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

    /** 接收用户 ID */
    private Long userId;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 通知类型（1-申请状态 2-审核结果 3-回访提醒 4-系统通知） */
    private Integer type;

    /** 关联业务类型（如 application / follow_up） */
    private String refType;

    /** 关联业务 ID */
    private Long refId;

}
