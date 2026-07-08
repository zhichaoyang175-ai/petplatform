package com.shanhai.petplatform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评价目标类型枚举
 *
 * @author PetPlatform Team
 */
@Getter
@AllArgsConstructor
public enum EvaluationTargetTypeEnum {

    USER(1, "用户"),
    SHELTER(2, "救助站");

    private final int code;
    private final String desc;

    public static EvaluationTargetTypeEnum fromCode(int code) {
        for (EvaluationTargetTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return USER;
    }

    /** 校验目标类型是否合法（1-用户 2-救助站） */
    public static boolean isValid(int code) {
        return code == USER.code || code == SHELTER.code;
    }
}
