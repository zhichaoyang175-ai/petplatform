package com.shanhai.petplatform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 养宠经验枚举
 *
 * @author PetPlatform Team
 */
@Getter
@AllArgsConstructor
public enum PetExperienceEnum {

    NONE(0, "无"),
    HAD(1, "有过"),
    CURRENTLY(2, "正在养");

    private final int code;
    private final String desc;

    public static PetExperienceEnum fromCode(int code) {
        for (PetExperienceEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return NONE;
    }

    public static PetExperienceEnum fromCode(int code, PetExperienceEnum defaultValue) {
        for (PetExperienceEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return defaultValue;
    }

}
