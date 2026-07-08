package com.shanhai.petplatform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型枚举
 *
 * @author PetPlatform Team
 */
@Getter
@AllArgsConstructor
public enum NotificationTypeEnum {

    APPLICATION_STATUS(1, "申请状态"),
    REVIEW_RESULT(2, "审核结果"),
    FOLLOW_UP_REMINDER(3, "回访提醒"),
    SYSTEM(4, "系统通知"),
    REVIEW(5, "审核通知");

    private final int code;
    private final String desc;

    public static NotificationTypeEnum fromCode(int code) {
        for (NotificationTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return SYSTEM;
    }

    public static NotificationTypeEnum fromCode(int code, NotificationTypeEnum defaultValue) {
        for (NotificationTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return defaultValue;
    }

}
