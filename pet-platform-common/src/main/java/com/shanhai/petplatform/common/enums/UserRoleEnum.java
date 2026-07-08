package com.shanhai.petplatform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 *
 * @author PetPlatform Team
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    APPLICANT(1, "领养人"),
    ADOPTER(2, "送养人"),
    ADMIN(3, "管理员"),
    REVIEWER(4, "审核员");

    private final int code;
    private final String desc;

    public static UserRoleEnum fromCode(int code) {
        for (UserRoleEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return APPLICANT;
    }

    public static UserRoleEnum fromCode(int code, UserRoleEnum defaultValue) {
        for (UserRoleEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return defaultValue;
    }

}
