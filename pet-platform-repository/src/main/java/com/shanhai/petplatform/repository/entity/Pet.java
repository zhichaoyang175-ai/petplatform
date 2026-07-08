package com.shanhai.petplatform.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宠物信息实体 — t_pet
 *
 * @author PetPlatform Team
 */
@Data
@TableName("t_pet")
public class Pet implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 宠物ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 送养人ID */
    private Long ownerId;

    /** 宠物名称 */
    private String name;

    /** 品种 */
    private String breed;

    /** 性别: 0-未知 1-公 2-母 */
    private Integer gender;

    /** 月龄 */
    private Integer ageMonths;

    /** 体重(kg) */
    private BigDecimal weightKg;

    /** 是否绝育: 0-否 1-是 */
    private Integer neutered;

    /** 健康状态: 1-健康 2-轻微疾病 3-治疗中 4-残疾 */
    private Integer healthStatus;

    /** 所在省 */
    private String locationProvince;

    /** 所在市 */
    private String locationCity;

    /** 详细描述 */
    private String description;

    /** 领养要求 */
    private String adoptionRequirements;

    /** 状态: 0-待领养 1-申请中 2-审核中 3-已领养 4-回访中 5-已完成 6-已下架 */
    private Integer status;

    /** 浏览次数 */
    private Integer viewCount;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
