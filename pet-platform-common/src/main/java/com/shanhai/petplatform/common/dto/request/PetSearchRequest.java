package com.shanhai.petplatform.common.dto.request;

import lombok.Data;

/**
 * 宠物搜索请求 — 所有字段均为可选，组合筛选
 *
 * @author PetPlatform Team
 */
@Data
public class PetSearchRequest {

    /** 品种 */
    private String breed;

    /** 最小月龄 */
    private Integer ageMin;

    /** 最大月龄 */
    private Integer ageMax;

    /** 性别 */
    private Integer gender;

    /** 是否绝育 */
    private Integer neutered;

    /** 健康状态 */
    private Integer healthStatus;

    /** 所在省 */
    private String province;

    /** 所在市 */
    private String city;

    /** 状态（默认查询待领养） */
    private Integer status;

    /** 当前页码 */
    private Integer page = 1;

    /** 每页大小 */
    private Integer size = 10;

    /** 排序字段: created_at / view_count */
    private String sortBy;

}
