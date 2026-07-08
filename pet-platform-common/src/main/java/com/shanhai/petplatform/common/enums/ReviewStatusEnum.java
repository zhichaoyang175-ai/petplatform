package com.shanhai.petplatform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核状态枚举（通用审核框架）
 *
 * @author PetPlatform Team
 */
@Getter
@AllArgsConstructor
public enum ReviewStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已驳回"),
    CANCELLED(3, "已取消");

    private final int code;
    private final String desc;

    public static ReviewStatusEnum fromCode(int code) {
        for (ReviewStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return PENDING;
    }
}
