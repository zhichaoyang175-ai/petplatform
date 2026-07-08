package com.shanhai.petplatform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 宠物状态枚举
 *
 * @author PetPlatform Team
 */
@Getter
@AllArgsConstructor
public enum PetStatusEnum {

    AVAILABLE(0, "待领养"),
    APPLIED(1, "申请中"),
    REVIEWING(2, "审核中"),
    ADOPTED(3, "已领养"),
    FOLLOW_UP(4, "回访中"),
    COMPLETED(5, "已完成"),
    OFFLINE(6, "已下架");

    private final int code;
    private final String desc;

    public static PetStatusEnum fromCode(int code) {
        for (PetStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return AVAILABLE;
    }

    public static PetStatusEnum fromCode(int code, PetStatusEnum defaultValue) {
        for (PetStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return defaultValue;
    }

}
