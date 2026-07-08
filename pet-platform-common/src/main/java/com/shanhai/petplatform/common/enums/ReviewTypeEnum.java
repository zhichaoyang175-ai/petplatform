package com.shanhai.petplatform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核类型枚举（通用审核框架，后续可扩展救助站认证等）
 *
 * @author PetPlatform Team
 */
@Getter
@AllArgsConstructor
public enum ReviewTypeEnum {

    ADOPTER_APPLY(1, "送养人认证"),
    SHELTER_APPLY(2, "救助站认证");

    private final int code;
    private final String desc;

    public static ReviewTypeEnum fromCode(int code) {
        for (ReviewTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return ADOPTER_APPLY;
    }
}
