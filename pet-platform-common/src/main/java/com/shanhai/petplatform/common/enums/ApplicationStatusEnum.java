package com.shanhai.petplatform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 领养申请状态枚举
 *
 * @author PetPlatform Team
 */
@Getter
@AllArgsConstructor
public enum ApplicationStatusEnum {

    PENDING(0, "待审核"),
    REVIEWING(1, "审核中"),
    APPROVED(2, "已通过"),
    REJECTED(3, "已驳回"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String desc;

    public static ApplicationStatusEnum fromCode(int code) {
        for (ApplicationStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return PENDING;
    }

    public static ApplicationStatusEnum fromCode(int code, ApplicationStatusEnum defaultValue) {
        for (ApplicationStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return defaultValue;
    }

}
