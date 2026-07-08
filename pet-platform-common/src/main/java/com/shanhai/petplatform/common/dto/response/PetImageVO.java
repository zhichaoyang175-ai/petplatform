package com.shanhai.petplatform.common.dto.response;

import lombok.Data;

/**
 * 宠物图片 VO
 *
 * @author PetPlatform Team
 */
@Data
public class PetImageVO {

    /** 图片ID */
    private Long id;

    /** 图片URL */
    private String imageUrl;

    /** 类型: 1-宠物照片 2-回访照片 */
    private Integer imageType;

    /** 排序 */
    private Integer sortOrder;

    /** 快速构建 */
    public static PetImageVO of(Long id, String imageUrl, Integer imageType, Integer sortOrder) {
        PetImageVO vo = new PetImageVO();
        vo.setId(id);
        vo.setImageUrl(imageUrl);
        vo.setImageType(imageType);
        vo.setSortOrder(sortOrder);
        return vo;
    }

}
