package com.shanhai.petplatform.common.dto.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 宠物更新请求 — 所有字段可选（部分更新）
 *
 * @author PetPlatform Team
 */
@Data
public class PetUpdateRequest {

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

    /** 健康状态 */
    private Integer healthStatus;

    /** 所在省 */
    private String locationProvince;

    /** 所在市 */
    private String locationCity;

    /** 详细描述 */
    private String description;

    /** 领养要求 */
    private String adoptionRequirements;

}
