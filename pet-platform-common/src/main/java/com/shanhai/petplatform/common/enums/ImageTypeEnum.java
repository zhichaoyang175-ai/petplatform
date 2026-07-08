package com.shanhai.petplatform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 图片类型枚举
 *
 * @author PetPlatform Team
 */
@Getter
@AllArgsConstructor
public enum ImageTypeEnum {

    PET_PHOTO(1, "宠物照片"),
    FOLLOW_UP_PHOTO(2, "回访照片");

    private final int code;
    private final String desc;

    public static ImageTypeEnum fromCode(int code) {
        for (ImageTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return PET_PHOTO;
    }

    public static ImageTypeEnum fromCode(int code, ImageTypeEnum defaultValue) {
        for (ImageTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return defaultValue;
    }

}
