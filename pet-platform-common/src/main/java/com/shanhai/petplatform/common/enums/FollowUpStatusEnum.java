package com.shanhai.petplatform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 回访状态枚举
 *
 * @author PetPlatform Team
 */
@Getter
@AllArgsConstructor
public enum FollowUpStatusEnum {

    PENDING(0, "待执行"),
    REMINDED(1, "已提醒"),
    COMPLETED(2, "已完成"),
    OVERDUE(3, "已逾期");

    private final int code;
    private final String desc;

    public static FollowUpStatusEnum fromCode(int code) {
        for (FollowUpStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return PENDING;
    }

    public static FollowUpStatusEnum fromCode(int code, FollowUpStatusEnum defaultValue) {
        for (FollowUpStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return defaultValue;
    }

}
