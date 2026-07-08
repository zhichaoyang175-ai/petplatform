package com.shanhai.petplatform.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 救助站创建/更新请求
 *
 * @author PetPlatform Team
 */
@Data
public class ShelterRequest {

    /** 救助站名称 */
    @NotBlank(message = "救助站名称不能为空")
    private String name;

    /** 简介/描述 */
    private String description;

    /** 详细地址 */
    private String address;

    /** 所在省 */
    private String province;

    /** 所在市 */
    private String city;

    /** 联系手机号 */
    private String contactPhone;

    /** LOGO/头像 URL */
    private String logoUrl;

}
