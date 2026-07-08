package com.shanhai.petplatform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 救助站状态枚举
 *
 * @author PetPlatform Team
 */
@Getter
@AllArgsConstructor
public enum ShelterStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已驳回");

    private final int code;
    private final String desc;

    public static ShelterStatusEnum fromCode(int code) {
        for (ShelterStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return PENDING;
    }

    public static ShelterStatusEnum fromCode(int code, ShelterStatusEnum defaultValue) {
        for (ShelterStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return defaultValue;
    }

}
