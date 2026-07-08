package com.shanhai.petplatform.common.dto.request;

import lombok.Data;

/**
 * 分页请求基类 — 所有分页查询 DTO 可继承此类
 *
 * @author PetPlatform Team
 */
@Data
public class PageDTO {

    /** 当前页码 */
    private int page = 1;

    /** 每页大小 */
    private int size = 10;

}
