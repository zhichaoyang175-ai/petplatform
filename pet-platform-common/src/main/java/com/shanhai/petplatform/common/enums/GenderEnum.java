package com.shanhai.petplatform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 *
 * @author PetPlatform Team
 */
@Getter
@AllArgsConstructor
public enum GenderEnum {

    UNKNOWN(0, "未知"),
    MALE(1, "公"),
    FEMALE(2, "母");

    private final int code;
    private final String desc;

    public static GenderEnum fromCode(int code) {
        for (GenderEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return UNKNOWN;
    }

    public static GenderEnum fromCode(int code, GenderEnum defaultValue) {
        for (GenderEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return defaultValue;
    }

}
