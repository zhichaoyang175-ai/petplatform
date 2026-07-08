package com.shanhai.petplatform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 住房类型枚举
 *
 * @author PetPlatform Team
 */
@Getter
@AllArgsConstructor
public enum HousingTypeEnum {

    OWN(1, "自有房"),
    RENT(2, "租房"),
    FAMILY(3, "与家人同住");

    private final int code;
    private final String desc;

    public static HousingTypeEnum fromCode(int code) {
        for (HousingTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return OWN;
    }

    public static HousingTypeEnum fromCode(int code, HousingTypeEnum defaultValue) {
        for (HousingTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return defaultValue;
    }

}
