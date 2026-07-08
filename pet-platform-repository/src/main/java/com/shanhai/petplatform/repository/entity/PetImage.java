package com.shanhai.petplatform.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 宠物图片实体 — t_pet_image
 *
 * @author PetPlatform Team
 */
@Data
@TableName("t_pet_image")
public class PetImage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 图片ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 宠物ID */
    private Long petId;

    /** 图片URL/路径 */
    private String imageUrl;

    /** 类型: 1-宠物照片 2-回访照片 */
    private Integer imageType;

    /** 排序 */
    private Integer sortOrder;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

}
