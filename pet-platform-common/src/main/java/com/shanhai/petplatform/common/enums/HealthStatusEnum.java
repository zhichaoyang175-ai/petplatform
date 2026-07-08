package com.shanhai.petplatform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 健康状态枚举
 *
 * @author PetPlatform Team
 */
@Getter
@AllArgsConstructor
public enum HealthStatusEnum {

    HEALTHY(1, "健康"),
    MILD_ILLNESS(2, "轻微疾病"),
    TREATING(3, "治疗中"),
    DISABLED(4, "残疾");

    private final int code;
    private final String desc;

    public static HealthStatusEnum fromCode(int code) {
        for (HealthStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return HEALTHY;
    }

    public static HealthStatusEnum fromCode(int code, HealthStatusEnum defaultValue) {
        for (HealthStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return defaultValue;
    }

}
